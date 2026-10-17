package com.focusflow.feature.limits

import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.model.DailyAppLimit
import com.focusflow.core.domain.blocking.model.RemoteDetectionSelectors
import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import com.focusflow.core.domain.blocking.repository.AdvancedBlockingRepository
import com.focusflow.core.domain.blocking.repository.AppLimitRepository
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import com.focusflow.core.domain.blocking.repository.StudyModeRepository
import com.focusflow.core.domain.usage.model.AppCategory
import com.focusflow.core.domain.usage.model.InstalledApp
import com.focusflow.core.domain.usage.repository.AppListRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LimitsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeAppListRepository : AppListRepository {
        val appsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
        val essentialsFlow = MutableStateFlow<Set<String>>(emptySet())

        override fun getInstalledApps(): Flow<List<InstalledApp>> = appsFlow
        override fun getUserEssentials(): Flow<Set<String>> = essentialsFlow
        override suspend fun setUserEssential(packageName: String, isEssential: Boolean) {
            val current = essentialsFlow.value.toMutableSet()
            if (isEssential) current.add(packageName) else current.remove(packageName)
            essentialsFlow.value = current
        }
    }

    private class FakeBlockRuleRepository : BlockRuleRepository {
        val rulesFlow = MutableStateFlow<List<AppBlockRule>>(emptyList())
        val schedulesFlow = MutableStateFlow<List<BlockSchedule>>(emptyList())

        override fun observeAllRules(): Flow<List<AppBlockRule>> = rulesFlow
        override fun observeRuleForPackage(packageName: String): Flow<AppBlockRule?> =
            flowOf(rulesFlow.value.find { it.packageName == packageName })

        override suspend fun setRule(rule: AppBlockRule) {
            val updated = rulesFlow.value.filterNot { it.packageName == rule.packageName } + rule
            rulesFlow.value = updated
        }

        override suspend fun removeRule(packageName: String) {
            rulesFlow.value = rulesFlow.value.filterNot { it.packageName == packageName }
        }

        override fun observeAllSchedules(): Flow<List<BlockSchedule>> = schedulesFlow
        override suspend fun upsertSchedule(schedule: BlockSchedule) {}
        override suspend fun deleteSchedule(scheduleId: String) {}
    }

    private class FakeAppLimitRepository : AppLimitRepository {
        val limitsFlow = MutableStateFlow<List<DailyAppLimit>>(emptyList())

        override fun observeAllLimits(): Flow<List<DailyAppLimit>> = limitsFlow
        override fun observeLimitForPackage(packageName: String): Flow<DailyAppLimit?> =
            flowOf(limitsFlow.value.find { it.packageName == packageName })

        override suspend fun setLimit(limit: DailyAppLimit) {
            val updated = limitsFlow.value.filterNot { it.packageName == limit.packageName } + limit
            limitsFlow.value = updated
        }

        override suspend fun removeLimit(packageName: String) {
            limitsFlow.value = limitsFlow.value.filterNot { it.packageName == packageName }
        }
    }

    private class FakeAdvancedBlockingRepository : AdvancedBlockingRepository {
        val configFlow = MutableStateFlow(AdvancedBlockingConfig())
        override fun getConfig(): Flow<AdvancedBlockingConfig> = configFlow
        override suspend fun setShortsBlockingEnabled(enabled: Boolean): Result<Unit> = Result.success(Unit)
        override suspend fun setReelsBlockingEnabled(enabled: Boolean): Result<Unit> = Result.success(Unit)
        override suspend fun setBlockDuringFocusOnly(focusOnly: Boolean): Result<Unit> = Result.success(Unit)
        override fun getRemoteSelectors(): Flow<RemoteDetectionSelectors> = flowOf(RemoteDetectionSelectors())
    }

    private class FakeStudyModeRepository : StudyModeRepository {
        val configFlow = MutableStateFlow(StudyModeConfig())
        val channelsFlow = MutableStateFlow<List<StudyChannel>>(emptyList())
        override fun getStudyModeConfig(): Flow<StudyModeConfig> = configFlow
        override suspend fun setStudyModeEnabled(enabled: Boolean): Result<Unit> = Result.success(Unit)
        override suspend fun setFailClosed(failClosed: Boolean): Result<Unit> = Result.success(Unit)
        override fun getAllowlistedChannels(): Flow<List<StudyChannel>> = channelsFlow
        override suspend fun addChannel(channel: StudyChannel): Result<Unit> {
            channelsFlow.value = channelsFlow.value + channel
            return Result.success(Unit)
        }
        override suspend fun removeChannel(channelId: String): Result<Unit> {
            channelsFlow.value = channelsFlow.value.filterNot { it.channelId == channelId }
            return Result.success(Unit)
        }
        override suspend fun toggleChannelAllowlist(channelId: String, isAllowlisted: Boolean): Result<Unit> =
            Result.success(Unit)
    }

    private lateinit var appListRepo: FakeAppListRepository
    private lateinit var blockRuleRepo: FakeBlockRuleRepository
    private lateinit var appLimitRepo: FakeAppLimitRepository
    private lateinit var advancedRepo: FakeAdvancedBlockingRepository
    private lateinit var studyModeRepo: FakeStudyModeRepository
    private lateinit var viewModel: LimitsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appListRepo = FakeAppListRepository()
        blockRuleRepo = FakeBlockRuleRepository()
        appLimitRepo = FakeAppLimitRepository()
        advancedRepo = FakeAdvancedBlockingRepository()
        studyModeRepo = FakeStudyModeRepository()

        appListRepo.appsFlow.value = listOf(
            InstalledApp("com.instagram.android", "Instagram", isSafeListed = false, isEssential = false, category = AppCategory.SOCIAL),
            InstalledApp("com.android.dialer", "Phone", isSafeListed = true, isEssential = true, category = AppCategory.OTHER),
            InstalledApp("com.google.android.youtube", "YouTube", isSafeListed = false, isEssential = false, category = AppCategory.ENTERTAINMENT),
        )

        viewModel = LimitsViewModel(
            appListRepository = appListRepo,
            blockRuleRepository = blockRuleRepo,
            appLimitRepository = appLimitRepo,
            advancedBlockingRepository = advancedRepo,
            studyModeRepository = studyModeRepo,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsInstalledAppsWithCorrectSafeListStatus() = runTest {
        val state = viewModel.uiState.first { !it.isLoading && it.apps.isNotEmpty() }
        assertEquals(3, state.apps.size)

        val phone = state.apps.first { it.packageName == "com.android.dialer" }
        assertTrue(phone.isSafeListed)

        val insta = state.apps.first { it.packageName == "com.instagram.android" }
        assertFalse(insta.isSafeListed)
    }

    @Test
    fun onSearchQueryChanged_filtersAppsByLabelAndPackage() = runTest {
        viewModel.onSearchQueryChanged("insta")
        val state = viewModel.uiState.first { it.searchQuery == "insta" }

        assertEquals(1, state.apps.size)
        assertEquals("Instagram", state.apps.first().appName)
    }

    @Test
    fun onToggleBlockDuringFocus_updatesRepositoryRule() = runTest {
        viewModel.onToggleBlockDuringFocus("com.instagram.android", false)
        val rules = blockRuleRepo.rulesFlow.first { it.isNotEmpty() }

        val rule = rules.first { it.packageName == "com.instagram.android" }
        assertFalse(rule.blockDuringFocus)
    }

    @Test
    fun onSetDailyLimit_savesLimitMinutes() = runTest {
        viewModel.onSetDailyLimit("com.instagram.android", minutes = 45, launchLimit = 10)
        val limits = appLimitRepo.limitsFlow.first { it.isNotEmpty() }

        val limit = limits.first { it.packageName == "com.instagram.android" }
        assertEquals(45 * 60_000L, limit.dailyTimeLimitMs)
        assertEquals(10, limit.dailyLaunchLimit)

        val state = viewModel.uiState.first { state ->
            state.apps.any { it.packageName == "com.instagram.android" && it.dailyTimeLimitMinutes == 45 }
        }
        val insta = state.apps.first { it.packageName == "com.instagram.android" }
        assertEquals(45, insta.dailyTimeLimitMinutes)
    }

    @Test
    fun onSetDailyLimit_null_clearsLimit() = runTest {
        viewModel.onSetDailyLimit("com.instagram.android", minutes = null, launchLimit = null)
        val limits = appLimitRepo.limitsFlow.first { it.isNotEmpty() }

        val limit = limits.first { it.packageName == "com.instagram.android" }
        assertNull(limit.dailyTimeLimitMs)
        assertNull(limit.dailyLaunchLimit)
    }

    @Test
    fun studyChannels_filtersOutCuratedDefaults() = runTest {
        studyModeRepo.channelsFlow.value = listOf(
            StudyChannel(channelId = "c1", channelTitle = "Khan Academy", isCuratedDefault = true),
            StudyChannel(channelId = "c2", channelTitle = "Custom Math", isCuratedDefault = false),
        )

        val state = viewModel.uiState.first { it.studyChannels.isNotEmpty() }
        assertEquals(1, state.studyChannels.size)
        assertEquals("Custom Math", state.studyChannels.first().channelTitle)
    }

    @Test
    fun onRemoveStudyChannel_removesChannel() = runTest {
        studyModeRepo.channelsFlow.value = listOf(
            StudyChannel(channelId = "c2", channelTitle = "Custom Math", isCuratedDefault = false),
        )

        viewModel.onRemoveStudyChannel("c2")
        val remaining = studyModeRepo.channelsFlow.first { it.isEmpty() }
        assertTrue(remaining.isEmpty())
    }
}
