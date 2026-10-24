package com.focusflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class FocusMode {
    WORK,
    STUDY,
    PERSONAL
}

/**
 * UI state for the Home screen.
 *
 * Immutable data class representing the complete screen state.
 * Per ARCHITECTURE.md: "Unidirectional data flow. One immutable UI state object per screen."
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val title: String = "FocusFlow",
    val subtitle: String = "M1 Foundation ✓",
    val initializedAtEpochMs: Long = 0L,
    val selectedMode: FocusMode = FocusMode.WORK,
    val todayFocusTimeMinutes: Int = 0,
    val currentStreakDays: Int = 0,
    val selectedDurationMinutes: Int = 25,
    val availableDurations: List<Int> = listOf(25, 50, 90),
    val weeklyFocusFormatted: String = "0m",
    val weeklyGoalPercent: Int = 0,
    val weeklyDailyHours: List<Pair<String, Float>> = listOf(
        "Mon" to 0f,
        "Tue" to 0f,
        "Wed" to 0f,
        "Thu" to 0f,
        "Fri" to 0f,
        "Sat" to 0f,
        "Sun" to 0f,
    ),
    val greeting: String = "Good Morning ☀️",
    val quote: String = "A focused mind can achieve anything.",
    val activeSession: FocusSession? = null,
    val isSessionActive: Boolean = false,
    val isSessionRunning: Boolean = false,
    val isSessionPaused: Boolean = false,
    val sessionRemainingMs: Long = 0L,
    val sessionElapsedMs: Long = 0L,
    val sessionProgress: Float = 0f,
) {
    val formattedSessionTime: String
        get() {
            val totalSeconds = (sessionRemainingMs / 1000L).coerceAtLeast(0L)
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            val hours = minutes / 60L
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60L, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
}

/**
 * ViewModel for the Home screen.
 *
 * Single source of truth for the focus session lives in [FocusSessionRepository]
 * shared by UI and service.
 * "Time is stored as timestamps, never as tick counts. Inject a clock."
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository,
    private val clock: Clock,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(initializedAtEpochMs = clock.now())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var latestCompletedSessions: List<FocusSession> = emptyList()
    private var latestWeeklyGoalHours: Int = 16
    private var latestUserProfile: UserProfile? = null

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            focusSessionRepository.currentSession,
            focusSessionRepository.observeAllCompletedSessions(),
            userPreferencesRepository.getWeeklyGoalHours(),
            authRepository.userProfile,
        ) { session, completedSessions, weeklyGoal, profile ->
            latestCompletedSessions = completedSessions
            latestWeeklyGoalHours = weeklyGoal
            latestUserProfile = profile

            val now = clock.now()
            val isRunning = session?.state == SessionState.RUNNING
            val isPaused = session?.state == SessionState.PAUSED
            val isActive = session?.state?.isActive == true
            val elapsed = session?.elapsedDurationMs(now) ?: 0L
            val remaining = session?.remainingDurationMs(now) ?: 0L
            val progress = session?.progressFraction(now) ?: 0f

            val sessionMinutes = session?.let { s ->
                when (val mode = s.mode) {
                    is SessionMode.Pomodoro -> (mode.workDurationMs / 60_000L).toInt()
                    is SessionMode.Custom -> (mode.durationMs / 60_000L).toInt()
                    else -> null
                }
            }

            val stats = calculateWeeklyStats(
                now = now,
                completedSessions = completedSessions,
                activeSession = session,
                weeklyGoalHours = weeklyGoal,
                userProfile = profile,
            )

            _uiState.update { current ->
                current.copy(
                    activeSession = session,
                    isSessionActive = isActive,
                    isSessionRunning = isRunning,
                    isSessionPaused = isPaused,
                    sessionRemainingMs = remaining,
                    sessionElapsedMs = elapsed,
                    sessionProgress = progress,
                    selectedDurationMinutes = sessionMinutes ?: current.selectedDurationMinutes,
                    weeklyFocusFormatted = stats.weeklyFocusFormatted,
                    weeklyGoalPercent = stats.weeklyGoalPercent,
                    weeklyDailyHours = stats.weeklyDailyHours,
                    todayFocusTimeMinutes = stats.todayFocusTimeMinutes,
                    currentStreakDays = stats.currentStreakDays,
                    greeting = stats.greeting,
                    quote = stats.quote,
                )
            }

            if (session != null && isRunning) {
                startTicker(session)
            } else {
                stopTicker()
            }
        }.launchIn(viewModelScope)
    }

    private fun startTicker(session: FocusSession) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val now = clock.now()
                val elapsed = session.elapsedDurationMs(now)
                val remaining = session.remainingDurationMs(now)
                val progress = session.progressFraction(now)

                val stats = calculateWeeklyStats(
                    now = now,
                    completedSessions = latestCompletedSessions,
                    activeSession = session,
                    weeklyGoalHours = latestWeeklyGoalHours,
                    userProfile = latestUserProfile,
                )

                _uiState.update { current ->
                    current.copy(
                        sessionElapsedMs = elapsed,
                        sessionRemainingMs = remaining,
                        sessionProgress = progress,
                        weeklyFocusFormatted = stats.weeklyFocusFormatted,
                        weeklyGoalPercent = stats.weeklyGoalPercent,
                        weeklyDailyHours = stats.weeklyDailyHours,
                        todayFocusTimeMinutes = stats.todayFocusTimeMinutes,
                        currentStreakDays = stats.currentStreakDays,
                    )
                }

                if (session.isTargetReached(now)) {
                    completeFocus()
                    break
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun calculateWeeklyStats(
        now: Long,
        completedSessions: List<FocusSession>,
        activeSession: FocusSession?,
        weeklyGoalHours: Int,
        userProfile: UserProfile?,
    ): DynamicWeeklyStats {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), zoneId)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val startOfWeekMs = monday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfWeekMs = monday.plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val thisWeekSessions = completedSessions.filter {
            it.startedAtEpochMs in startOfWeekMs until endOfWeekMs
        }

        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        var totalWeekMs = 0L
        var todayFocusMs = 0L

        val dailyHours = (0L..6L).map { dayOffset ->
            val date = monday.plusDays(dayOffset)
            val dayStartMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEndMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            var dayMs = thisWeekSessions
                .filter { it.startedAtEpochMs in dayStartMs until dayEndMs }
                .sumOf { s ->
                    s.elapsedDurationMs(now).coerceAtLeast(0L)
                }

            if (date == today && activeSession != null && activeSession.state.isActive) {
                dayMs += activeSession.elapsedDurationMs(now).coerceAtLeast(0L)
            }

            if (date == today) {
                todayFocusMs = dayMs
            }
            totalWeekMs += dayMs

            val hours = dayMs.toFloat() / (1000f * 60f * 60f)
            dayLabels[dayOffset.toInt()] to hours
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

        val targetGoalMinutes = (weeklyGoalHours * 60).coerceAtLeast(1)
        val weeklyGoalPercent = ((totalWeekMinutes.toFloat() / targetGoalMinutes.toFloat()) * 100)
            .toInt()
            .coerceIn(0, 100)

        val todayFocusTimeMinutes = if (todayFocusMs > 0L && todayFocusMs < 60_000L) 1 else (todayFocusMs / 60_000L).toInt()

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

        val currentStreakDays = maxOf(streak, userProfile?.currentStreakDays ?: 0)

        val hour = LocalTime.ofInstant(Instant.ofEpochMilli(now), zoneId).hour
        val greeting = when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon ☀️"
            in 17..21 -> "Good Evening 🌙"
            else -> "Good Night 🌙"
        }

        val quotes = listOf(
            "A focused mind can achieve anything.",
            "Small daily disciplines lead to massive long-term results.",
            "Deep work is the superpower of the modern age.",
            "Your focus determines your reality.",
            "Energy flows where attention goes.",
            "Simplicity is the prerequisite for focus.",
            "Great acts are made up of small daily efforts.",
        )
        val quoteIndex = (today.toEpochDay() % quotes.size).toInt().let { if (it < 0) it + quotes.size else it }
        val quote = quotes[quoteIndex]

        return DynamicWeeklyStats(
            weeklyFocusFormatted = weeklyFocusFormatted,
            weeklyGoalPercent = weeklyGoalPercent,
            weeklyDailyHours = dailyHours,
            todayFocusTimeMinutes = todayFocusTimeMinutes,
            currentStreakDays = currentStreakDays,
            greeting = greeting,
            quote = quote,
        )
    }

    fun selectMode(mode: FocusMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun selectDuration(minutes: Int) {
        if (_uiState.value.isSessionActive) return
        _uiState.update { it.copy(selectedDurationMinutes = minutes) }
    }

    fun startFocus(customMinutes: Int? = null) {
        viewModelScope.launch {
            val minutes = customMinutes ?: _uiState.value.selectedDurationMinutes
            val durationMs = minutes * 60 * 1000L
            val mode = if (minutes == 25) {
                SessionMode.Pomodoro(workDurationMs = durationMs)
            } else {
                SessionMode.Custom(durationMs)
            }
            if (customMinutes != null) {
                _uiState.update { it.copy(selectedDurationMinutes = minutes) }
            }
            focusSessionRepository.startSession(mode = mode)
        }
    }

    fun pauseFocus() {
        viewModelScope.launch {
            focusSessionRepository.pauseSession()
        }
    }

    fun resumeFocus() {
        viewModelScope.launch {
            focusSessionRepository.resumeSession()
        }
    }

    fun stopFocus() {
        viewModelScope.launch {
            focusSessionRepository.completeSession() ?: focusSessionRepository.abortSession()
            stopTicker()
        }
    }

    fun completeFocus() {
        viewModelScope.launch {
            focusSessionRepository.completeSession()
            stopTicker()
        }
    }
}

private data class DynamicWeeklyStats(
    val weeklyFocusFormatted: String,
    val weeklyGoalPercent: Int,
    val weeklyDailyHours: List<Pair<String, Float>>,
    val todayFocusTimeMinutes: Int,
    val currentStreakDays: Int,
    val greeting: String,
    val quote: String,
)
