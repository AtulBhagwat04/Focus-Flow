package com.focusflow.feature.profile

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import com.focusflow.core.domain.sync.model.SyncResult
import com.focusflow.core.domain.sync.repository.SyncRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        val userFlow = MutableStateFlow<UserAccount?>(UserAccount(uid = "test-uid", displayName = "Test User"))
        val profileFlow = MutableStateFlow<UserProfile?>(UserProfile(uid = "test-uid", currentStreakDays = 4, xp = 250L))

        override val currentUser: StateFlow<UserAccount?> = userFlow
        override val userProfile: Flow<UserProfile?> = profileFlow
        override suspend fun signInAnonymously(): Result<UserAccount> = Result.success(UserAccount("anon"))
        override suspend fun signInWithGoogle(idToken: String): Result<UserAccount> = Result.success(UserAccount("google"))
        override suspend fun linkGoogleAccount(idToken: String): Result<UserAccount> = Result.success(UserAccount("link"))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }

    private class FakeSyncRepository : SyncRepository {
        val syncingFlow = MutableStateFlow(false)
        override val isSyncing: Flow<Boolean> = syncingFlow
        override fun enqueueSync() {}
        override suspend fun performSync(): SyncResult = SyncResult.Success
        override suspend fun exportUserDataJson(): Result<String> = Result.success("{}")
    }

    private class FakeBlockRuleRepository : BlockRuleRepository {
        val rulesFlow = MutableStateFlow<List<AppBlockRule>>(emptyList())
        override fun observeAllRules(): Flow<List<AppBlockRule>> = rulesFlow
        override fun observeRuleForPackage(packageName: String): Flow<AppBlockRule?> = flowOf(null)
        override suspend fun setRule(rule: AppBlockRule) {}
        override suspend fun removeRule(packageName: String) {}
        override fun observeAllSchedules(): Flow<List<BlockSchedule>> = flowOf(emptyList())
        override suspend fun upsertSchedule(schedule: BlockSchedule) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
    }

    private class FakeFocusSessionRepository : FocusSessionRepository {
        val allCompletedSessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())
        val currentSessionFlow = MutableStateFlow<FocusSession?>(null)

        override val currentSession: Flow<FocusSession?> = currentSessionFlow
        override suspend fun startSession(mode: SessionMode, tagId: String?, customTargetDurationMs: Long?): FocusSession = throw NotImplementedError()
        override suspend fun pauseSession(): FocusSession? = null
        override suspend fun resumeSession(): FocusSession? = null
        override suspend fun completeSession(): FocusSession? = null
        override suspend fun abortSession(): FocusSession? = null
        override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> = allCompletedSessionsFlow
        override fun observeFocusDurationBetween(startEpochMs: Long, endEpochMs: Long): Flow<Long> = flowOf(0L)
        override fun observeCompletedSessionsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<FocusSession>> = allCompletedSessionsFlow
        override fun observeCompletedSessionCount(): Flow<Int> = flowOf(allCompletedSessionsFlow.value.size)
        override fun observeAllCompletedSessions(): Flow<List<FocusSession>> = allCompletedSessionsFlow
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        val goalFlow = MutableStateFlow(20)
        val remindersFlow = MutableStateFlow(true)
        val appearanceFlow = MutableStateFlow("Dark")

        override fun getWeeklyGoalHours(): Flow<Int> = goalFlow
        override suspend fun setWeeklyGoalHours(hours: Int) { goalFlow.value = hours }
        override fun getRemindersEnabled(): Flow<Boolean> = remindersFlow
        override suspend fun setRemindersEnabled(enabled: Boolean) { remindersFlow.value = enabled }
        override fun getAppearanceTheme(): Flow<String> = appearanceFlow
        override suspend fun setAppearanceTheme(theme: String) { appearanceFlow.value = theme }
    }

    private val fakeClock = object : Clock {
        override fun now(): Long = 1_700_000_000_000L
    }

    private lateinit var authRepo: FakeAuthRepository
    private lateinit var syncRepo: FakeSyncRepository
    private lateinit var blockRepo: FakeBlockRuleRepository
    private lateinit var focusRepo: FakeFocusSessionRepository
    private lateinit var prefsRepo: FakeUserPreferencesRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepo = FakeAuthRepository()
        syncRepo = FakeSyncRepository()
        blockRepo = FakeBlockRuleRepository()
        focusRepo = FakeFocusSessionRepository()
        prefsRepo = FakeUserPreferencesRepository()

        // Set up 5 completed sessions, totaling 120 minutes this week
        val baseTime = 1_700_000_000_000L
        val sessions = listOf(
            FocusSession(
                id = "s1",
                mode = SessionMode.Custom(60 * 60 * 1000L),
                state = SessionState.COMPLETED,
                startedAtEpochMs = baseTime - 120 * 60 * 1000L,
                completedAtEpochMs = baseTime - 60 * 60 * 1000L,
                xpEarned = 60,
            ),
            FocusSession(
                id = "s2",
                mode = SessionMode.Custom(60 * 60 * 1000L),
                state = SessionState.COMPLETED,
                startedAtEpochMs = baseTime - 60 * 60 * 1000L,
                completedAtEpochMs = baseTime,
                xpEarned = 60,
            ),
            FocusSession(
                id = "s3",
                mode = SessionMode.Pomodoro(),
                state = SessionState.COMPLETED,
                startedAtEpochMs = baseTime - 86400_000L * 1, // Yesterday
                completedAtEpochMs = baseTime - 86400_000L * 1 + 25 * 60 * 1000L,
                xpEarned = 25,
            ),
            FocusSession(
                id = "s4",
                mode = SessionMode.Pomodoro(),
                state = SessionState.COMPLETED,
                startedAtEpochMs = baseTime - 86400_000L * 2,
                completedAtEpochMs = baseTime - 86400_000L * 2 + 25 * 60 * 1000L,
                xpEarned = 25,
            ),
            FocusSession(
                id = "s5",
                mode = SessionMode.Pomodoro(),
                state = SessionState.COMPLETED,
                startedAtEpochMs = baseTime - 86400_000L * 3,
                completedAtEpochMs = baseTime - 86400_000L * 3 + 25 * 60 * 1000L,
                xpEarned = 25,
            ),
        )
        focusRepo.allCompletedSessionsFlow.value = sessions

        viewModel = ProfileViewModel(
            authRepository = authRepo,
            syncRepository = syncRepo,
            blockRuleRepository = blockRepo,
            focusSessionRepository = focusRepo,
            userPreferencesRepository = prefsRepo,
            clock = fakeClock,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_reflectsDynamicWeeklyFocus_andSessionsCount() = runTest {
        val state = viewModel.uiState.first()

        // 170 total weekly minutes for the sessions within this week (Nov 13 Monday onwards)
        assertTrue(state.weeklyFocusMinutes > 0L)
        // 5 completed sessions
        assertEquals(5, state.completedSessionsCount)
        // 0 blocked apps initially
        assertEquals(0, state.blockedAppsCount)
        // 20 hours weekly goal from prefsRepo
        assertEquals(20, state.weeklyGoalHours)
        // Streak is dynamic from sessions and profile
        assertTrue(state.currentStreakDays >= 4)
    }

    @Test
    fun updateWeeklyGoal_updatesPreferencesRepository() = runTest {
        viewModel.onUpdateWeeklyGoal(24)
        assertEquals(24, prefsRepo.goalFlow.value)
    }

    @Test
    fun toggleReminders_updatesPreferencesRepository() = runTest {
        viewModel.onToggleReminders(false)
        assertFalse(prefsRepo.remindersFlow.value)
    }

    @Test
    fun selectAppearance_updatesPreferencesRepository() = runTest {
        viewModel.onSelectAppearance("Botanical")
        assertEquals("Botanical", prefsRepo.appearanceFlow.value)
    }
}
