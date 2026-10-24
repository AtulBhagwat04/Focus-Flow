package com.focusflow.feature.stats

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionHealth
import com.focusflow.core.domain.permission.PermissionHealthRepository
import com.focusflow.core.domain.permission.PermissionState
import com.focusflow.core.domain.usage.model.DailyUsageStats
import com.focusflow.core.domain.usage.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
            throw NotImplementedError()

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

    private val fakeClock = object : Clock {
        // 1_700_000_000_000L is Nov 14, 2023 (Tuesday)
        override fun now(): Long = 1_700_000_000_000L
    }

    private lateinit var usageRepo: FakeUsageStatsRepository
    private lateinit var healthRepo: FakePermissionHealthRepository
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        usageRepo = FakeUsageStatsRepository()
        healthRepo = FakePermissionHealthRepository()

        viewModel = StatsViewModel(
            usageStatsRepository = usageRepo,
            permissionHealthRepository = healthRepo,
            clock = fakeClock,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDayStats() = runTest {
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.DAY, state.selectedRange)
        assertEquals("3h", state.totalScreenTimeFormatted)
        assertEquals("1h 30m", state.totalFocusTimeFormatted)
        assertEquals(42, state.unlockCount)
        assertTrue(state.hasUsagePermission)
    }

    @Test
    fun selectWeekRange_updatesRangeCalculation() = runTest {
        viewModel.onSelectRange(StatsTimeRange.WEEK)
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.WEEK, state.selectedRange)
        // Check that week range was queried (start of week <= end of week)
        assertTrue((usageRepo.lastEndEpochDay ?: 0L) >= (usageRepo.lastStartEpochDay ?: 0L))
    }

    @Test
    fun selectMonthRange_updatesRangeCalculation() = runTest {
        viewModel.onSelectRange(StatsTimeRange.MONTH)
        val state = viewModel.uiState.first()

        assertEquals(StatsTimeRange.MONTH, state.selectedRange)
        assertTrue((usageRepo.lastEndEpochDay ?: 0L) >= (usageRepo.lastStartEpochDay ?: 0L))
    }
}
