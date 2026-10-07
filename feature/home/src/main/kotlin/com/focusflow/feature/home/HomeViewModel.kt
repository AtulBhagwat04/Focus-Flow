package com.focusflow.feature.home

import androidx.lifecycle.ViewModel
import com.focusflow.core.common.time.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

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
}
