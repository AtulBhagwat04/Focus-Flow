package com.focusflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val weeklyFocusFormatted: String = "12h 30m",
    val weeklyGoalPercent: Int = 78,
    val weeklyDailyHours: List<Pair<String, Float>> = listOf(
        "Mon" to 3.5f,
        "Tue" to 1.8f,
        "Wed" to 3.8f,
        "Thu" to 2.8f,
        "Fri" to 4.8f,
        "Sat" to 2.2f,
        "Sun" to 3.2f,
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(initializedAtEpochMs = clock.now())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        observeSession()
    }

    private fun observeSession() {
        focusSessionRepository.currentSession
            .onEach { session ->
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
                    )
                }

                if (isRunning && session != null) {
                    startTicker(session)
                } else {
                    stopTicker()
                }
            }
            .launchIn(viewModelScope)
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

                _uiState.update { current ->
                    current.copy(
                        sessionElapsedMs = elapsed,
                        sessionRemainingMs = remaining,
                        sessionProgress = progress,
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
            focusSessionRepository.abortSession()
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

