package com.focusflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import com.focusflow.core.domain.sync.model.SyncResult
import com.focusflow.core.domain.sync.repository.SyncRepository
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val blockRuleRepository: BlockRuleRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {

    private val exportedData = MutableStateFlow<String?>(null)
    private val isExporting = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val customDisplayName = MutableStateFlow<String?>(null)
    private val tickNow = MutableStateFlow(clock.now())
    private var tickerJob: Job? = null

    private fun updateTicker(isRunning: Boolean) {
        if (isRunning && tickerJob == null) {
            tickerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000L)
                    tickNow.value = clock.now()
                }
            }
        } else if (!isRunning && tickerJob != null) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            authRepository.currentUser,
            authRepository.userProfile,
            syncRepository.isSyncing,
            exportedData,
            isExporting,
        ) { user, profile, syncing, exportJson, exporting ->
            ProfileBaseData(user, profile, syncing, exportJson, exporting)
        },
        combine(
            userMessage,
            userPreferencesRepository.getRemindersEnabled(),
            userPreferencesRepository.getAppearanceTheme(),
            userPreferencesRepository.getWeeklyGoalHours(),
            blockRuleRepository.observeAllRules(),
        ) { message, reminders, appearance, goal, rules ->
            ProfileSettingsData(message, reminders, appearance, goal, rules.size)
        },
        combine(
            focusSessionRepository.currentSession,
            focusSessionRepository.observeAllCompletedSessions(),
            customDisplayName,
            tickNow,
        ) { activeSession, completedSessions, customName, now ->
            val isRunning = activeSession?.state == SessionState.RUNNING
            updateTicker(isRunning)

            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), zoneId)
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val startOfWeekMs = monday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfWeekMs = monday.plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val thisWeekSessions = completedSessions.filter {
                it.startedAtEpochMs in startOfWeekMs until endOfWeekMs
            }
            var totalWeekMs = thisWeekSessions.sumOf { s ->
                s.elapsedDurationMs(now).coerceAtLeast(0L)
            }
            if (activeSession != null && activeSession.state.isActive) {
                totalWeekMs += activeSession.elapsedDurationMs(now).coerceAtLeast(0L)
            }

            val totalWeekMinutes = totalWeekMs / 60_000L
            val weeklyHours = totalWeekMinutes / 60L
            val weeklyMins = totalWeekMinutes % 60L
            val weeklyFocusFormatted = when {
                weeklyHours > 0L && weeklyMins > 0L -> "${weeklyHours}h ${weeklyMins}m"
                weeklyHours > 0L -> "${weeklyHours}h"
                weeklyMins > 0L -> "${weeklyMins}m"
                totalWeekMs > 0L -> "<1m"
                else -> "0m"
            }

            val sessionDates = completedSessions.map {
                LocalDate.ofInstant(Instant.ofEpochMilli(it.startedAtEpochMs), zoneId)
            }.toSet()

            var streak = 0
            var checkDate = if (sessionDates.contains(today)) {
                today
            } else if (sessionDates.contains(today.minusDays(1))) {
                today.minusDays(1)
            } else {
                null
            }

            while (checkDate != null && sessionDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }

            val totalXp = completedSessions.sumOf { it.xpEarned.toLong() }

            ProfileStatsData(
                weeklyFocusMinutes = totalWeekMinutes,
                weeklyFocusFormatted = weeklyFocusFormatted,
                completedSessionsCount = completedSessions.size,
                streakDays = streak,
                totalXp = totalXp,
                customDisplayName = customName,
            )
        },
    ) { base, settings, stats ->
        val effectiveAccount = if (stats.customDisplayName != null && base.user != null) {
            base.user.copy(displayName = stats.customDisplayName)
        } else if (stats.customDisplayName != null) {
            UserAccount(uid = "local", displayName = stats.customDisplayName, isAnonymous = false)
        } else {
            base.user
        }

        val effectiveXp = maxOf(base.profile?.xp ?: 0L, stats.totalXp)
        val effectiveLevel = maxOf(base.profile?.level ?: 1, 1 + (effectiveXp / 1000L).toInt())
        val effectiveStreak = maxOf(base.profile?.currentStreakDays ?: 0, stats.streakDays)

        ProfileUiState(
            account = effectiveAccount,
            profile = base.profile,
            isSyncing = base.syncing,
            exportedDataJson = base.exportJson,
            isExporting = base.exporting,
            message = settings.message,
            isLoading = false,
            weeklyFocusMinutes = stats.weeklyFocusMinutes,
            weeklyFocusFormatted = stats.weeklyFocusFormatted,
            completedSessionsCount = stats.completedSessionsCount,
            currentStreakDays = effectiveStreak,
            level = effectiveLevel,
            xp = effectiveXp,
            weeklyGoalHours = settings.goal,
            blockedAppsCount = settings.rulesCount,
            remindersEnabled = settings.reminders,
            appearanceTheme = settings.appearance,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState(isLoading = true),
    )

    init {
        // Auto-initialize anonymous account if no user is signed in
        viewModelScope.launch {
            authRepository.signInAnonymously()
        }
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            userMessage.value = "Starting sync..."
            when (val result = syncRepository.performSync()) {
                is SyncResult.Success -> userMessage.value = "Sync completed successfully."
                is SyncResult.Failure -> userMessage.value = "Sync failed: ${result.errorMessage}"
            }
        }
    }

    fun onExportDataClicked() {
        viewModelScope.launch {
            isExporting.value = true
            syncRepository.exportUserDataJson().fold(
                onSuccess = { json ->
                    exportedData.value = json
                    userMessage.value = "Data export generated."
                },
                onFailure = { error ->
                    userMessage.value = "Export failed: ${error.message}"
                },
            )
            isExporting.value = false
        }
    }

    fun onDismissExport() {
        exportedData.value = null
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.signInAnonymously()
        }
    }

    fun onDeleteAccountClicked() {
        viewModelScope.launch {
            authRepository.deleteAccount().fold(
                onSuccess = {
                    userMessage.value = "Account and data deleted."
                    authRepository.signInAnonymously()
                },
                onFailure = { error ->
                    userMessage.value = "Deletion failed: ${error.message}"
                },
            )
        }
    }

    fun onClearMessage() {
        userMessage.value = null
    }

    fun onToggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRemindersEnabled(enabled)
        }
    }

    fun onSelectAppearance(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAppearanceTheme(theme)
        }
    }

    fun onUpdateWeeklyGoal(hours: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setWeeklyGoalHours(hours)
        }
    }

    fun onUpdateDisplayName(name: String) {
        customDisplayName.value = name
        userMessage.value = "Name updated."
    }
}

private data class ProfileBaseData(
    val user: UserAccount?,
    val profile: UserProfile?,
    val syncing: Boolean,
    val exportJson: String?,
    val exporting: Boolean,
)

private data class ProfileSettingsData(
    val message: String?,
    val reminders: Boolean,
    val appearance: String,
    val goal: Int,
    val rulesCount: Int,
)

private data class ProfileStatsData(
    val weeklyFocusMinutes: Long,
    val weeklyFocusFormatted: String,
    val completedSessionsCount: Int,
    val streakDays: Int,
    val totalXp: Long,
    val customDisplayName: String?,
)
