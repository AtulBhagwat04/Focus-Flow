package com.focusflow.feature.home.onboarding

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    @Test
    fun initialUiState_hasStep1AndDefaults() = runBlocking {
        val viewModel = OnboardingViewModel()
        val state = viewModel.uiState.first()

        assertEquals(1, state.currentStep)
        assertEquals(5, state.totalSteps)
        assertEquals(OnboardingGoal.STUDY, state.selectedGoal)
        assertEquals(DailyFocusTarget.MIN_15, state.selectedDailyTarget)
        assertFalse(state.isCompleted)
    }

    @Test
    fun nextStep_advancesThroughSteps() = runBlocking {
        val viewModel = OnboardingViewModel()

        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.first().currentStep)

        viewModel.nextStep()
        assertEquals(3, viewModel.uiState.first().currentStep)

        viewModel.nextStep()
        assertEquals(4, viewModel.uiState.first().currentStep)

        viewModel.nextStep()
        assertEquals(5, viewModel.uiState.first().currentStep)

        // Calling next on last step completes onboarding
        viewModel.nextStep()
        assertTrue(viewModel.uiState.first().isCompleted)
    }

    @Test
    fun previousStep_navigatesBackwards() = runBlocking {
        val viewModel = OnboardingViewModel()
        viewModel.nextStep() // at step 2
        viewModel.nextStep() // at step 3

        val canGoBack = viewModel.previousStep()
        assertTrue(canGoBack)
        assertEquals(2, viewModel.uiState.first().currentStep)

        val canGoBackAgain = viewModel.previousStep()
        assertTrue(canGoBackAgain)
        assertEquals(1, viewModel.uiState.first().currentStep)

        // Cannot go back from step 1
        val cannotGoBack = viewModel.previousStep()
        assertFalse(cannotGoBack)
        assertEquals(1, viewModel.uiState.first().currentStep)
    }

    @Test
    fun skip_completesOnboarding() = runBlocking {
        val viewModel = OnboardingViewModel()
        viewModel.skip()
        assertTrue(viewModel.uiState.first().isCompleted)
    }

    @Test
    fun selectGoal_updatesSelectedGoal() = runBlocking {
        val viewModel = OnboardingViewModel()

        viewModel.selectGoal(OnboardingGoal.WORK)
        assertEquals(OnboardingGoal.WORK, viewModel.uiState.first().selectedGoal)

        viewModel.selectGoal(OnboardingGoal.HABITS)
        assertEquals(OnboardingGoal.HABITS, viewModel.uiState.first().selectedGoal)
    }

    @Test
    fun toggleDistraction_togglesItemsInSet() = runBlocking {
        val viewModel = OnboardingViewModel()

        // DistractionType.GAMES is unselected by default
        viewModel.toggleDistraction(DistractionType.GAMES)
        assertTrue(viewModel.uiState.first().selectedDistractions.contains(DistractionType.GAMES))

        // Toggle again to remove
        viewModel.toggleDistraction(DistractionType.GAMES)
        assertFalse(viewModel.uiState.first().selectedDistractions.contains(DistractionType.GAMES))
    }

    @Test
    fun selectDailyTarget_updatesTarget() = runBlocking {
        val viewModel = OnboardingViewModel()

        viewModel.selectDailyTarget(DailyFocusTarget.MIN_30)
        assertEquals(DailyFocusTarget.MIN_30, viewModel.uiState.first().selectedDailyTarget)

        viewModel.selectDailyTarget(DailyFocusTarget.MIN_60)
        assertEquals(DailyFocusTarget.MIN_60, viewModel.uiState.first().selectedDailyTarget)
    }
}
