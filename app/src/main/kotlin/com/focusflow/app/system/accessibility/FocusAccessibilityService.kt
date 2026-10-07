package com.focusflow.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focusflow.app.system.blocking.BlockerOverlayManager
import com.focusflow.core.domain.blocking.engine.BlockingRulesEngine
import com.focusflow.core.domain.blocking.engine.BlockingSignals
import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.BlockReason
import com.focusflow.core.domain.blocking.model.BlockingDecision
import com.focusflow.core.domain.blocking.model.RemoteDetectionSelectors
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import com.focusflow.core.domain.blocking.repository.AdvancedBlockingRepository
import com.focusflow.core.domain.blocking.repository.StudyModeRepository
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val PKG_YOUTUBE = "com.google.android.youtube"
private const val PKG_INSTAGRAM = "com.instagram.android"
private const val ACTION_DEBOUNCE_MS = 800L

@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject lateinit var advancedBlockingRepository: AdvancedBlockingRepository
    @Inject lateinit var studyModeRepository: StudyModeRepository
    @Inject lateinit var focusSessionRepository: FocusSessionRepository
    @Inject lateinit var blockerOverlayManager: BlockerOverlayManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var advancedConfig = AdvancedBlockingConfig()
    private var studyConfig = StudyModeConfig()
    private var remoteSelectors = RemoteDetectionSelectors()
    private var sessionState = SessionState.IDLE

    private var lastActionTimestamp = 0L

    private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as? PowerManager }
    private val keyguardManager by lazy { getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager }

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceScope.launch {
            advancedBlockingRepository.getConfig().collect {
                advancedConfig = it
            }
        }

        serviceScope.launch {
            studyModeRepository.getStudyModeConfig().collect {
                studyConfig = it
            }
        }

        serviceScope.launch {
            advancedBlockingRepository.getRemoteSelectors().collect {
                remoteSelectors = it
            }
        }

        serviceScope.launch {
            focusSessionRepository.getActiveSession().collect { session ->
                sessionState = session?.state ?: SessionState.IDLE
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        // Strictly target only YouTube and Instagram
        if (packageName != PKG_YOUTUBE && packageName != PKG_INSTAGRAM) return

        // Pause monitoring when screen is off or device locked
        if (powerManager?.isInteractive == false || keyguardManager?.isDeviceLocked == true) return

        // Respect remote kill switch
        if (remoteSelectors.killSwitchEnabled) return

        val now = System.currentTimeMillis()
        if (now - lastActionTimestamp < ACTION_DEBOUNCE_MS) return

        val rootNode = rootInActiveWindow ?: return

        try {
            var isFeedDetected = false
            var detectedChannelName: String? = null

            // 1. Shorts / Reels Detection using Remote Config identifiers
            val targetViewIds = if (packageName == PKG_YOUTUBE) {
                remoteSelectors.youtubeShortsViewIds
            } else {
                remoteSelectors.instagramReelsViewIds
            }

            for (viewId in targetViewIds) {
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
                if (nodes.isNotEmpty()) {
                    isFeedDetected = true
                    nodes.forEach { it.recycle() }
                    break
                }
            }

            // Fallback content-description check if view IDs shift dynamically
            if (!isFeedDetected) {
                val desc = event.contentDescription?.toString()?.lowercase() ?: ""
                val text = event.text.joinToString().lowercase()
                if (desc.contains("shorts") || desc.contains("reels") || text.contains("shorts") || text.contains("reels")) {
                    isFeedDetected = true
                }
            }

            // 2. YouTube Study Mode: inspect video channel name (ephemeral, zero logging)
            if (packageName == PKG_YOUTUBE && studyConfig.isEnabled && !isFeedDetected) {
                detectedChannelName = findYouTubeChannelName(rootNode)
            }

            // 3. Evaluate with pure domain Rules Engine
            val calendar = Calendar.getInstance()
            val signals = BlockingSignals(
                packageName = packageName,
                nowEpochMs = now,
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE),
                sessionState = sessionState,
                isShortFormFeedDetected = isFeedDetected,
                detectedChannelName = detectedChannelName,
                advancedBlockingConfig = advancedConfig,
                studyModeConfig = studyConfig,
            )

            val decision = BlockingRulesEngine.evaluate(signals)

            if (decision is BlockingDecision.Block) {
                lastActionTimestamp = now
                when (decision.reason) {
                    BlockReason.SHORT_FORM_FEED -> {
                        // Gentle exit: system back action redirects user out of short-form feed
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                    BlockReason.STUDY_MODE_CHANNEL_NOT_ALLOWLISTED -> {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        serviceScope.launch {
                            blockerOverlayManager.showOverlay(
                                packageName = packageName,
                                decision = decision,
                                onEmergencyPass = {},
                            )
                        }
                    }
                    else -> {
                        // Other reasons handled by ForegroundAppDetector overlay
                    }
                }
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun findYouTubeChannelName(root: AccessibilityNodeInfo): String? {
        val candidateNodes = root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/channel_title")
        if (candidateNodes.isNotEmpty()) {
            val name = candidateNodes.first().text?.toString()
            candidateNodes.forEach { it.recycle() }
            if (!name.isNullOrBlank()) return name
        }

        // Secondary check by channel sub-text or video subtitle container
        val subNodes = root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/owner_name")
        if (subNodes.isNotEmpty()) {
            val name = subNodes.first().text?.toString()
            subNodes.forEach { it.recycle() }
            if (!name.isNullOrBlank()) return name
        }

        return null
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by system
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
