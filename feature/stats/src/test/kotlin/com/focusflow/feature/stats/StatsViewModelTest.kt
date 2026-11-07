package com.focusflow.feature.stats

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionHealth
import com.focusflow.core.domain.permission.PermissionHealthRepository
import com.focusflow.core.domain.permission.PermissionState
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.UserPreferencesRepository
import com.focusflow.core.domain.usage.model.DailyUsageStats
import com.focusflow.core.domain.usage.repository.UsageStatsRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeUsageStatsRepository : UsageStatsRepository {
        var lastStartEpochDay: Long? = null
        var lastEndEpochDay: Long? = null
        val dailyStatsFlow = MutableStateFlow(
            DailyUsageStats(
                epochDay = 19675L,
                totalScreenTimeMs = 3 * 3600_000L, // 3h
                totalFocusTimeMs = 90 * 60_000L,   // 1h 30m
                unlockCount = 42,
                topApps = emptyList(),
            )
        )

        override fun observeDailyUsage(epochDay: Long): Flow<DailyUsageStats> = dailyStatsFlow

        override fun observeDateRangeUsage(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyUsageStats>> =
            flowOf(emptyList())

        override fun observeRangeUsage(startEpochDay: Long, endEpochDay: Long): Flow<DailyUsageStats> {
            lastStartEpochDay = startEpochDay
            lastEndEpochDay = endEpochDay
            return dailyStatsFlow
        }

        override suspend fun refreshUsageForDay(epochDay: Long) {}
    }

    private class FakePermissionHealthRepository : PermissionHealthRepository {
        val healthFlow = MutableStateFlow(
            PermissionHealth(
                statuses = mapOf(
                    AppPermission.USAGE_ACCESS to PermissionState.Granted
                )
            )
        )
        override val permissionHealth: Flow<PermissionHealth> = healthFlow
        override suspend fun refreshHealth() {}
    }

    private class FakeFocusSessionRepository : FocusSessionRepository {
        val completedSessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())
        val allCompletedSessionsFlow = MutableStateFlow<List<FocusSession>>(emptyList())
        val durationFlow = MutableStateFlow(0L)

        override val currentSession: Flow<FocusSession?> = flowOf(null)
        override suspend fun startSession(mode: SessionMode, tagId: String?, customTargetDurationMs: Long?): FocusSession = throw NotImplementedError()
        override suspend fun pauseSession(): FocusSession? = null
        override suspend fun resumeSession(): FocusSession? = null
        override suspend fun completeSession(): FocusSession? = null
        override suspend fun abortSession(): FocusSession? = null
        override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> = allCompletedSessionsFlow
        override fun observeFocusDurationBetween(startEpochMs: Long, endEpochMs: Long): Flow<Long> = durationFlow
        override fun observeCompletedSessionsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<FocusSession>> = completedSessionsFlow
        override fun observeCompletedSessionCount(): Flow<Int> = flowOf(allCompletedSessionsFlow.value.size)
        override fun observeAllCompletedSessions(): Flow<List<FocusSession>> = allCompletedSessionsFlow
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        val weeklyGoalHoursFlow = MutableStateFlow(16)
        override fun getWeeklyGoalHours(): Flow<Int> = weeklyGoalHoursFlow
        override suspend fun setWeeklyGoalHours(hours: Int) { weeklyGoalHoursFlow.value = hours }
        override fun getRemindersEnabled(): Flow<Boolean> = flowOf(true)
        override suspend fun setRemindersEnabled(enabled: Boolean) {}
        override fun getStrictFocusModeEnabled(): Flow<Boolean> = flowOf(false)
        override suspend fun setStrictFocusModeEnabled(enabled: Boolean) {}
        override fun getAppearanceTheme(): Flow<String> = flowOf("Light")
        override suspend fun setAppearanceTheme(theme: String) {}
    }

    private val fakeClock = object : Clock {
        // 1_700_000_000_000L is Nov 14, 2023 (Tuesday)
        override fun now(): Long = 1_700_000_000_000L
    }

    private lateinit var usageRepo: FakeUsageStatsRepository
    private lateinit var healthRepo: FakePermissionHealthRepository
    private lateinit var focusRepo: FakeFocusSessionRepository
    private lateinit var prefsRepo: FakeUserPreferencesRepository
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        usageRepo = FakeUsageStatsRepository()
        healthRepo = FakePermissionHealthRepository()
        focusRepo = FakeFocusSessionRepository()
        prefsRepo = FakeUserPreferencesRepository()

        viewModel = StatsViewModel(
            usageStatsRepository = usageRepo,
            permissionHealthRepository = healthRepo,
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
    fun initialState_loadsWeekStatsDefault() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.WEEK, state.selectedRange)
        assertEquals("3h", state.totalScreenTimeFormatted)
        assertEquals("1h 30m", state.totalFocusTimeFormatted)
        assertEquals(42, state.unlockCount)
        assertEquals("Focus each day", state.chartTitle)
        assertEquals(7, state.chartBars.size)
        assertTrue(state.hasUsagePermission)
        assertFalse(state.canNavigateNext)
    }

    @Test
    fun selectDayRange_updatesRangeCalculation() = runTest {
        viewModel.onSelectRange(StatsTimeRange.DAY)
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.DAY, state.selectedRange)
        assertEquals("Focus by hour", state.chartTitle)
        assertTrue((usageRepo.lastEndEpochDay ?: 0L) >= (usageRepo.lastStartEpochDay ?: 0L))
    }

    @Test
    fun selectMonthRange_updatesRangeCalculation() = runTest {
        viewModel.onSelectRange(StatsTimeRange.MONTH)
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.MONTH, state.selectedRange)
        assertEquals("Focus each week", state.chartTitle)
        assertTrue(state.chartBars.size in 4..5)
        assertTrue((usageRepo.lastEndEpochDay ?: 0L) >= (usageRepo.lastStartEpochDay ?: 0L))
    }


    @Test
    fun periodNavigation_updatesOffset() = runTest {
        viewModel.onPreviousPeriod()
        var state = viewModel.uiState.first()
        assertEquals(-1, state.periodOffset)
        assertTrue(state.canNavigateNext)

        viewModel.onNextPeriod()
        state = viewModel.uiState.first()
        assertEquals(0, state.periodOffset)
        assertFalse(state.canNavigateNext)
    }

    @Test
    fun completedSessions_computeHighlightsCorrectly() = runTest {
        // Nov 17, 2023 was a Friday
        val fridayEpochMs = 1700215200000L // 2023-11-17 10:00:00 UTC
        val session = FocusSession(
            id = "sess_1",
            mode = SessionMode.Custom(3 * 3600_000L),
            state = SessionState.COMPLETED,
            targetDurationMs = 3 * 3600_000L,
            startedAtEpochMs = fridayEpochMs,
            completedAtEpochMs = fridayEpochMs + 3 * 3600_000L,
        )

        focusRepo.completedSessionsFlow.value = listOf(session)
        val state = viewModel.uiState.first()

        assertEquals("Friday", state.highlights.bestDayName)
        assertEquals("Best day · 3h", state.highlights.bestDayCaption)
        assertEquals("3h", state.highlights.avgSessionLength)
    }

    @Test
    fun updatingWeeklyGoal_dynamicallyUpdatesGoalMetrics() = runTest {
        var state = viewModel.uiState.first()
        assertEquals("Weekly goal: 16h", state.goalLabel)

        prefsRepo.weeklyGoalHoursFlow.value = 21
        state = viewModel.uiState.first()
        assertEquals("Weekly goal: 21h", state.goalLabel)
        assertEquals(3.0f, state.dailyGoalHours, 0.01f)
        assertEquals("3h", state.dailyGoalFormatted)
    }
}
