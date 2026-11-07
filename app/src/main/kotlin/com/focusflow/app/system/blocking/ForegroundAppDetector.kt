package com.focusflow.app.system.blocking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.util.Log
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.blocking.engine.BlockingRulesEngine
import com.focusflow.core.domain.blocking.engine.BlockingSignals
import com.focusflow.core.domain.blocking.model.BlockingDecision
import com.focusflow.core.domain.blocking.repository.AppLimitRepository
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import com.focusflow.core.domain.blocking.repository.EmergencyPassRepository
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import com.focusflow.core.domain.usage.repository.AppListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ForegroundAppDetector"
private const val DETECTOR_POLL_INTERVAL_MS = 800L
private const val USAGE_LOOKBACK_WINDOW_MS = 300_000L // 5-minute lookback for active resumed activity

@Singleton
class ForegroundAppDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenStateReceiver: ScreenStateReceiver,
    private val overlayManager: BlockerOverlayManager,
    private val sessionRepository: FocusSessionRepository,
    private val blockRuleRepository: BlockRuleRepository,
    private val appLimitRepository: AppLimitRepository,
    private val emergencyPassRepository: EmergencyPassRepository,
    private val appListRepository: AppListRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val scope = CoroutineScope(Dispatchers.Default)
    private var detectionJob: Job? = null

    fun startMonitoring() {
        if (detectionJob != null) return
        Log.d(TAG, "startMonitoring initiated")

        detectionJob = scope.launch {
            screenStateReceiver.isScreenInteractive.collect { isInteractive ->
                if (isInteractive) {
                    runDetectionLoop()
                } else {
                    overlayManager.hideOverlay()
                }
            }
        }
    }

    fun stopMonitoring() {
        Log.d(TAG, "stopMonitoring initiated")
        detectionJob?.cancel()
        detectionJob = null
        overlayManager.hideOverlay()
    }

    private suspend fun CoroutineScope.runDetectionLoop() {
        while (isActive && screenStateReceiver.isScreenInteractive.value) {
            try {
                evaluateForegroundApp()
            } catch (e: Exception) {
                Log.e(TAG, "Error in evaluateForegroundApp loop", e)
            }
            delay(DETECTOR_POLL_INTERVAL_MS)
        }
    }

    private suspend fun evaluateForegroundApp() {
        if (usageStatsManager == null) {
            Log.w(TAG, "UsageStatsManager is null!")
            return
        }

        val nowMs = clock.now()
        val zoneId = ZoneId.systemDefault()
        val localDate = LocalDate.ofInstant(Instant.ofEpochMilli(nowMs), zoneId)
        val localTime = LocalTime.ofInstant(Instant.ofEpochMilli(nowMs), zoneId)
        val dayOfWeek = localDate.dayOfWeek.value // 1 = Monday .. 7 = Sunday
        val minuteOfDay = localTime.hour * 60 + localTime.minute

        val foregroundPackage = getForegroundPackage(nowMs) ?: return
        if (foregroundPackage == context.packageName) {
            overlayManager.hideOverlay()
            return
        }

        val session = sessionRepository.currentSession.first()
        val blockRule = blockRuleRepository.observeRuleForPackage(foregroundPackage).first()
        val limit = appLimitRepository.observeLimitForPackage(foregroundPackage).first()
        val allSchedules = blockRuleRepository.observeAllSchedules().first()
        val emergencyPass = emergencyPassRepository.emergencyPass.first()
        val essentials = appListRepository.getUserEssentials().first()
        val isStrictFocusMode = userPreferencesRepository.getStrictFocusModeEnabled().first()

        val isDefaultLauncher = isDefaultLauncher(foregroundPackage)
        val isDefaultDialer = isDefaultDialer(foregroundPackage)

        val signals = BlockingSignals(
            packageName = foregroundPackage,
            nowEpochMs = nowMs,
            dayOfWeek = dayOfWeek,
            minuteOfDay = minuteOfDay,
            sessionState = session?.state ?: SessionState.IDLE,
            remainingSessionDurationMs = session?.remainingDurationMs(nowMs),
            consumedDurationTodayMs = 0L,
            launchCountToday = 0,
            emergencyPass = emergencyPass,
            isDefaultLauncher = isDefaultLauncher,
            isDefaultDialer = isDefaultDialer,
            userEssentials = essentials,
            blockRule = blockRule,
            dailyLimit = limit,
            activeSchedules = allSchedules,
            isStrictFocusMode = isStrictFocusMode,
        )

        val decision = BlockingRulesEngine.evaluate(signals)
        Log.d(TAG, "Package: $foregroundPackage -> Decision: $decision (session=${session?.state}, strict=$isStrictFocusMode, rule=$blockRule)")

        when (decision) {
            is BlockingDecision.Block -> {
                overlayManager.showOverlay(
                    packageName = foregroundPackage,
                    decision = decision,
                    onEmergencyPass = { emergencyPassRepository.requestPass() },
                )
            }
            is BlockingDecision.Allow,
            is BlockingDecision.WarnNearLimit -> {
                overlayManager.hideOverlay()
            }
        }
    }

    private fun isDefaultLauncher(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == packageName
        } catch (_: Exception) {
            false
        }
    }

    private fun isDefaultDialer(packageName: String): Boolean {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.defaultDialerPackage == packageName
        } catch (_: Exception) {
            false
        }
    }

    private fun getForegroundPackage(nowMs: Long): String? {
        val manager = usageStatsManager ?: return null

        try {
            val usageEvents = manager.queryEvents(nowMs - USAGE_LOOKBACK_WINDOW_MS, nowMs)
            if (usageEvents != null) {
                var lastResumedPackage: String? = null
                var lastResumedTime = 0L
                val event = UsageEvents.Event()

                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                        event.eventType == 1 // MOVE_TO_FOREGROUND
                    ) {
                        if (event.timeStamp >= lastResumedTime) {
                            lastResumedTime = event.timeStamp
                            lastResumedPackage = event.packageName
                        }
                    }
                }

                if (!lastResumedPackage.isNullOrBlank()) {
                    return lastResumedPackage
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query usage events", e)
        }

        return null
    }
}
