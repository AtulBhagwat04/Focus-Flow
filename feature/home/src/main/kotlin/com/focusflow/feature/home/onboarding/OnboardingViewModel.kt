package com.focusflow.feature.home.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for managing the 5-step onboarding flow.
 *
 * Implements unidirectional data flow with immutable [OnboardingUiState].
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
        } else {
            completeOnboarding()
        }
    }

    fun previousStep(): Boolean {
        val current = _uiState.value.currentStep
        return if (current > 1) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1)
            true
        } else {
            false
        }
    }

    fun skip() {
        completeOnboarding()
    }

    fun selectGoal(goal: OnboardingGoal) {
        _uiState.value = _uiState.value.copy(selectedGoal = goal)
    }

    fun toggleDistraction(distraction: DistractionType) {
        val current = _uiState.value.selectedDistractions
        val updated = if (current.contains(distraction)) {
            current - distraction
        } else {
            current + distraction
        }
        _uiState.value = _uiState.value.copy(selectedDistractions = updated)
    }

    fun selectDailyTarget(target: DailyFocusTarget) {
        _uiState.value = _uiState.value.copy(selectedDailyTarget = target)
    }

    fun completeOnboarding() {
        _uiState.value = _uiState.value.copy(isCompleted = true)
    }
}
