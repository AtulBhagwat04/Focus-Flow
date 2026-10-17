package com.focusflow.feature.home

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeFocusSessionRepository : FocusSessionRepository {
        val sessionFlow = MutableStateFlow<FocusSession?>(null)
        override val currentSession: Flow<FocusSession?> = sessionFlow

        override suspend fun startSession(
            mode: SessionMode,
            tagId: String?,
            customTargetDurationMs: Long?,
        ): FocusSession {
            val target = customTargetDurationMs ?: when (mode) {
                is SessionMode.Pomodoro -> mode.workDurationMs
                is SessionMode.Custom -> mode.durationMs
                else -> 25 * 60 * 1000L
            }
            val session = FocusSession(
                id = "test-session-id",
                mode = mode,
                state = SessionState.RUNNING,
                tagId = tagId,
                targetDurationMs = target,
                startedAtEpochMs = 1_700_000_000_000L,
            )
            sessionFlow.value = session
            return session
        }

        override suspend fun pauseSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val paused = current.copy(state = SessionState.PAUSED, pausedAtEpochMs = 1_700_000_600_000L)
            sessionFlow.value = paused
            return paused
        }

        override suspend fun resumeSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val resumed = current.copy(state = SessionState.RUNNING, pausedAtEpochMs = null)
            sessionFlow.value = resumed
            return resumed
        }

        override suspend fun completeSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val completed = current.copy(
                state = SessionState.COMPLETED,
                completedAtEpochMs = 1_700_001_500_000L,
                xpEarned = 25,
            )
            sessionFlow.value = null
            return completed
        }

        override suspend fun abortSession(): FocusSession? {
            sessionFlow.value = null
            return null
        }

        override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> = flowOf(emptyList())
    }

    private val fakeClock = object : Clock {
        override fun now(): Long = 1_700_000_000_000L
    }

    private lateinit var fakeRepo: FakeFocusSessionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeFocusSessionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            focusSessionRepository = fakeRepo,
            clock = fakeClock,
        )
    }

    @Test
    fun initialUiState_hasDefaultValues() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
        assertEquals("FocusFlow", state.title)
        assertEquals("M1 Foundation ✓", state.subtitle)
        assertEquals(1_700_000_000_000L, state.initializedAtEpochMs)
        assertEquals(FocusMode.WORK, state.selectedMode)
        assertEquals(25, state.selectedDurationMinutes)
        assertEquals(listOf(25, 50, 90), state.availableDurations)
        assertEquals(78, state.weeklyGoalPercent)
        assertFalse(state.isSessionActive)
        assertFalse(state.isSessionRunning)
        assertFalse(state.isSessionPaused)
        assertNull(state.activeSession)
    }

    @Test
    fun selectMode_updatesSelectedMode() = runTest {
        val viewModel = createViewModel()
        viewModel.selectMode(FocusMode.STUDY)
        assertEquals(FocusMode.STUDY, viewModel.uiState.first().selectedMode)

        viewModel.selectMode(FocusMode.PERSONAL)
        assertEquals(FocusMode.PERSONAL, viewModel.uiState.first().selectedMode)

        viewModel.selectMode(FocusMode.WORK)
        assertEquals(FocusMode.WORK, viewModel.uiState.first().selectedMode)
    }

    @Test
    fun selectDuration_updatesSelectedDuration() = runTest {
        val viewModel = createViewModel()
        viewModel.selectDuration(50)
        assertEquals(50, viewModel.uiState.first().selectedDurationMinutes)

        viewModel.selectDuration(90)
        assertEquals(90, viewModel.uiState.first().selectedDurationMinutes)

        viewModel.selectDuration(45)
        assertEquals(45, viewModel.uiState.first().selectedDurationMinutes)
    }

    @Test
    fun startFocus_withSelectedDuration_startsSessionAndReflectsInUiState() = runTest {
        val viewModel = createViewModel()
        viewModel.selectDuration(50)
        viewModel.startFocus()

        val state = viewModel.uiState.first()
        assertTrue(state.isSessionActive)
        assertTrue(state.isSessionRunning)
        assertFalse(state.isSessionPaused)
        assertNotNull(state.activeSession)
        assertEquals(50, state.selectedDurationMinutes)
        assertEquals(50 * 60 * 1000L, state.sessionRemainingMs)
        assertEquals("50:00", state.formattedSessionTime)
    }

    @Test
    fun startFocus_withCustomDuration_startsCustomSessionAndUpdatesState() = runTest {
        val viewModel = createViewModel()
        viewModel.startFocus(customMinutes = 35)

        val state = viewModel.uiState.first()
        assertTrue(state.isSessionActive)
        assertTrue(state.isSessionRunning)
        assertEquals(35, state.selectedDurationMinutes)
        assertEquals(35 * 60 * 1000L, state.sessionRemainingMs)
        assertEquals("35:00", state.formattedSessionTime)
    }

    @Test
    fun pauseFocus_and_resumeFocus_updatesSessionStateCorrectly() = runTest {
        val viewModel = createViewModel()
        viewModel.startFocus()

        viewModel.pauseFocus()
        val pausedState = viewModel.uiState.first()
        assertTrue(pausedState.isSessionActive)
        assertFalse(pausedState.isSessionRunning)
        assertTrue(pausedState.isSessionPaused)

        viewModel.resumeFocus()
        val resumedState = viewModel.uiState.first()
        assertTrue(resumedState.isSessionActive)
        assertTrue(resumedState.isSessionRunning)
        assertFalse(resumedState.isSessionPaused)
    }

    @Test
    fun stopFocus_abortsSessionAndReturnsToIdle() = runTest {
        val viewModel = createViewModel()
        viewModel.startFocus()
        assertTrue(viewModel.uiState.first().isSessionActive)

        viewModel.stopFocus()
        val state = viewModel.uiState.first()
        assertFalse(state.isSessionActive)
        assertFalse(state.isSessionRunning)
        assertFalse(state.isSessionPaused)
        assertNull(state.activeSession)
    }

    @Test
    fun completeFocus_completesSessionAndReturnsToIdle() = runTest {
        val viewModel = createViewModel()
        viewModel.startFocus()
        assertTrue(viewModel.uiState.first().isSessionActive)

        viewModel.completeFocus()
        val state = viewModel.uiState.first()
        assertFalse(state.isSessionActive)
        assertFalse(state.isSessionRunning)
        assertNull(state.activeSession)
    }

    @Test
    fun externalSessionChanges_synchronizeToHomeUiState() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.first().isSessionActive)

        // Simulate FocusScreen starting a 90m session
        fakeRepo.startSession(mode = SessionMode.Custom(90 * 60 * 1000L))

        val state = viewModel.uiState.first()
        assertTrue(state.isSessionActive)
        assertTrue(state.isSessionRunning)
        assertEquals(90, state.selectedDurationMinutes)
        assertEquals("1:30:00", state.formattedSessionTime)

        // Simulate FocusScreen pausing
        fakeRepo.pauseSession()
        val pausedState = viewModel.uiState.first()
        assertTrue(pausedState.isSessionPaused)
        assertFalse(pausedState.isSessionRunning)

        // Simulate FocusScreen aborting
        fakeRepo.abortSession()
        val idleState = viewModel.uiState.first()
        assertFalse(idleState.isSessionActive)
    }
}
