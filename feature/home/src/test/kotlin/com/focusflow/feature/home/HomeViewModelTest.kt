package com.focusflow.feature.home

import com.focusflow.core.common.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HomeViewModelTest {

    private val fakeClock = object : Clock {
        override fun now(): Long = 1_700_000_000_000L
    }

    @Test
    fun initialUiState_hasDefaultValues() = runBlocking {
        val viewModel = HomeViewModel(clock = fakeClock)
        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertEquals("FocusFlow", state.title)
        assertEquals("M1 Foundation ✓", state.subtitle)
        assertEquals(1_700_000_000_000L, state.initializedAtEpochMs)
        assertEquals(FocusMode.WORK, state.selectedMode)
    }

    @Test
    fun selectMode_updatesSelectedMode() = runBlocking {
        val viewModel = HomeViewModel(clock = fakeClock)
        viewModel.selectMode(FocusMode.STUDY)
        assertEquals(FocusMode.STUDY, viewModel.uiState.first().selectedMode)

        viewModel.selectMode(FocusMode.PERSONAL)
        assertEquals(FocusMode.PERSONAL, viewModel.uiState.first().selectedMode)

        viewModel.selectMode(FocusMode.WORK)
        assertEquals(FocusMode.WORK, viewModel.uiState.first().selectedMode)
    }
}
