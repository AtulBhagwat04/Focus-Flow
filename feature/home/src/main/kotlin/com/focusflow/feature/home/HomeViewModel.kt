package com.focusflow.feature.home

import androidx.lifecycle.ViewModel
import com.focusflow.core.common.time.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

/**
 * ViewModel for the Home screen.
 *
 * Injects [Clock] from core:common to follow the rule:
 * "Time is stored as timestamps, never as tick counts. Inject a clock."
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(initializedAtEpochMs = clock.now())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectMode(mode: FocusMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun selectDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(selectedDurationMinutes = minutes)
    }
}

