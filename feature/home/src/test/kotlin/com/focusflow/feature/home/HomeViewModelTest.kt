package com.focusflow.feature.home

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeFocusSessionRepository : FocusSessionRepository {
        val sessionFlow = MutableStateFlow<FocusSession?>(null)
        val completedSessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())

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
            completedSessionsFlow.value = completedSessionsFlow.value + completed
            return completed
        }

        override suspend fun abortSession(): FocusSession? {
            sessionFlow.value = null
            return null
        }

        override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> = completedSessionsFlow

        override fun observeFocusDurationBetween(startEpochMs: Long, endEpochMs: Long): Flow<Long> =
            flowOf(0L)

        override fun observeCompletedSessionsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<FocusSession>> =
            completedSessionsFlow

        override fun observeCompletedSessionCount(): Flow<Int> =
            flowOf(completedSessionsFlow.value.size)

        override fun observeAllCompletedSessions(): Flow<List<FocusSession>> =
            completedSessionsFlow
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        val weeklyGoalHoursFlow = MutableStateFlow(16)
        val remindersFlow = MutableStateFlow(true)
        val appearanceFlow = MutableStateFlow("Light")

        override fun getWeeklyGoalHours(): Flow<Int> = weeklyGoalHoursFlow
        override suspend fun setWeeklyGoalHours(hours: Int) { weeklyGoalHoursFlow.value = hours }
        override fun getRemindersEnabled(): Flow<Boolean> = remindersFlow
        override suspend fun setRemindersEnabled(enabled: Boolean) { remindersFlow.value = enabled }
        override fun getAppearanceTheme(): Flow<String> = appearanceFlow
        override suspend fun setAppearanceTheme(theme: String) { appearanceFlow.value = theme }
    }

    private class FakeAuthRepository : AuthRepository {
        val userProfileFlow = MutableStateFlow<UserProfile?>(null)
        val currentUserFlow = MutableStateFlow<UserAccount?>(null)

        override val currentUser: StateFlow<UserAccount?> = currentUserFlow
        override val userProfile: Flow<UserProfile?> = userProfileFlow
        override suspend fun signInAnonymously(): Result<UserAccount> = Result.success(UserAccount("uid"))
        override suspend fun signInWithGoogle(idToken: String): Result<UserAccount> = Result.success(UserAccount("uid"))
        override suspend fun linkGoogleAccount(idToken: String): Result<UserAccount> = Result.success(UserAccount("uid"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }

    // 1_700_000_000_000L is Tuesday, Nov 14, 2023 UTC
    private val fakeClock = object : Clock {
        override fun now(): Long = 1_700_000_000_000L
    }

    private lateinit var fakeRepo: FakeFocusSessionRepository
    private lateinit var fakePrefsRepo: FakeUserPreferencesRepository
    private lateinit var fakeAuthRepo: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeFocusSessionRepository()
        fakePrefsRepo = FakeUserPreferencesRepository()
        fakeAuthRepo = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            focusSessionRepository = fakeRepo,
            clock = fakeClock,
            userPreferencesRepository = fakePrefsRepo,
            authRepository = fakeAuthRepo,
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
        assertEquals(0, state.weeklyGoalPercent)
        assertEquals("0m", state.weeklyFocusFormatted)
        assertEquals(0, state.todayFocusTimeMinutes)
        assertEquals(0, state.currentStreakDays)
        assertEquals(7, state.weeklyDailyHours.size)
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
    fun stopFocus_finishesSessionAndPreservesWeeklyFocusTime() = runTest {
        val viewModel = createViewModel()
        viewModel.startFocus()
        assertTrue(viewModel.uiState.first().isSessionActive)

        viewModel.stopFocus()
        val state = viewModel.uiState.first()
        assertFalse(state.isSessionActive)
        assertFalse(state.isSessionRunning)
        assertFalse(state.isSessionPaused)
        assertNull(state.activeSession)
        assertEquals("25m", state.weeklyFocusFormatted)
        assertEquals(25, state.todayFocusTimeMinutes)
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
    fun completedSessions_dynamicallyUpdatesWeeklyFocusAndStreak() = runTest {
        val now = 1_700_000_000_000L
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), zoneId)
        val todayStartMs = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Completed 60m session today
        val session1 = FocusSession(
            id = "sess-1",
            mode = SessionMode.Custom(60 * 60 * 1000L),
            state = SessionState.COMPLETED,
            targetDurationMs = 60 * 60 * 1000L,
            startedAtEpochMs = todayStartMs + 1000L,
            completedAtEpochMs = todayStartMs + 3600_000L,
            totalPausedDurationMs = 0L,
        )

        fakeRepo.completedSessionsFlow.value = listOf(session1)

        val viewModel = createViewModel()
        val state = viewModel.uiState.first()

        assertEquals("1h", state.weeklyFocusFormatted)
        assertEquals(60, state.todayFocusTimeMinutes)
        assertEquals(1, state.currentStreakDays)
        // 1h out of 16h weekly goal = 6%
        assertEquals(6, state.weeklyGoalPercent)
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
