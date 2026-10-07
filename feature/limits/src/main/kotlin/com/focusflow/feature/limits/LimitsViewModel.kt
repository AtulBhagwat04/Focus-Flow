package com.focusflow.feature.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.model.DailyAppLimit
import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import com.focusflow.core.domain.blocking.repository.AdvancedBlockingRepository
import com.focusflow.core.domain.blocking.repository.AppLimitRepository
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import com.focusflow.core.domain.blocking.repository.StudyModeRepository
import com.focusflow.core.domain.usage.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val MILLIS_PER_MINUTE = 60_000L

private data class LimitsBaseData(
    val query: String,
    val apps: List<AppLimitUiItem>,
    val schedules: List<BlockSchedule>,
)

private data class AdvancedBlockingData(
    val advancedConfig: AdvancedBlockingConfig,
    val studyConfig: StudyModeConfig,
    val studyChannels: List<StudyChannel>,
)

@HiltViewModel
class LimitsViewModel @Inject constructor(
    private val appListRepository: AppListRepository,
    private val blockRuleRepository: BlockRuleRepository,
    private val appLimitRepository: AppLimitRepository,
    private val advancedBlockingRepository: AdvancedBlockingRepository,
    private val studyModeRepository: StudyModeRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val userMessage = MutableStateFlow<String?>(null)

    private val baseLimitsFlow = combine(
        searchQuery,
        appListRepository.getInstalledApps(),
        blockRuleRepository.observeAllRules(),
        appLimitRepository.observeAllLimits(),
        blockRuleRepository.observeAllSchedules(),
    ) { query, installedApps, rules, limits, schedules ->
        val ruleMap = rules.associateBy { it.packageName }
        val limitMap = limits.associateBy { it.packageName }

        val filtered = installedApps
            .filter { app ->
                query.isBlank() || app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
            .map { app ->
                val rule = ruleMap[app.packageName]
                val limit = limitMap[app.packageName]
                val limitMinutes = limit?.dailyTimeLimitMs?.let { (it / MILLIS_PER_MINUTE).toInt() }

                AppLimitUiItem(
                    packageName = app.packageName,
                    appName = app.label,
                    isSafeListed = app.isSafeListed,
                    isEssential = app.isEssential,
                    blockDuringFocus = rule?.blockDuringFocus ?: true,
                    dailyTimeLimitMinutes = limitMinutes,
                    dailyLaunchLimit = limit?.dailyLaunchLimit,
                )
            }

        LimitsBaseData(
            query = query,
            apps = filtered,
            schedules = schedules,
        )
    }

    private val advancedFlow = combine(
        advancedBlockingRepository.getConfig(),
        studyModeRepository.getStudyModeConfig(),
        studyModeRepository.getAllowlistedChannels(),
    ) { advConfig, studyConfig, channels ->
        AdvancedBlockingData(
            advancedConfig = advConfig,
            studyConfig = studyConfig,
            studyChannels = channels,
        )
    }

    val uiState: StateFlow<LimitsUiState> = combine(
        baseLimitsFlow,
        advancedFlow,
        userMessage,
    ) { baseData, advData, msg ->
        LimitsUiState(
            searchQuery = baseData.query,
            apps = baseData.apps,
            schedules = baseData.schedules,
            advancedBlockingConfig = advData.advancedConfig,
            studyModeConfig = advData.studyConfig,
            studyChannels = advData.studyChannels,
            isAccessibilityEnabled = true, // Detected dynamically by PermissionChecker
            message = msg,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LimitsUiState(isLoading = true),
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onToggleBlockDuringFocus(packageName: String, blockDuringFocus: Boolean) {
        viewModelScope.launch {
            blockRuleRepository.setRule(
                AppBlockRule(
                    packageName = packageName,
                    blockDuringFocus = blockDuringFocus,
                    isAlwaysBlocked = false,
                )
            )
        }
    }

    fun onSetDailyLimit(packageName: String, minutes: Int?, launchLimit: Int?) {
        viewModelScope.launch {
            val millis = minutes?.let { it * MILLIS_PER_MINUTE }
            appLimitRepository.setLimit(
                DailyAppLimit(
                    packageName = packageName,
                    dailyTimeLimitMs = millis,
                    dailyLaunchLimit = launchLimit,
                )
            )
        }
    }

    fun onToggleEssential(packageName: String, isEssential: Boolean) {
        viewModelScope.launch {
            appListRepository.setUserEssential(packageName, isEssential)
        }
    }

    fun onToggleShorts(enabled: Boolean) {
        viewModelScope.launch {
            advancedBlockingRepository.setShortsBlockingEnabled(enabled)
        }
    }

    fun onToggleReels(enabled: Boolean) {
        viewModelScope.launch {
            advancedBlockingRepository.setReelsBlockingEnabled(enabled)
        }
    }

    fun onToggleFocusOnly(focusOnly: Boolean) {
        viewModelScope.launch {
            advancedBlockingRepository.setBlockDuringFocusOnly(focusOnly)
        }
    }

    fun onToggleStudyMode(enabled: Boolean) {
        viewModelScope.launch {
            studyModeRepository.setStudyModeEnabled(enabled)
        }
    }

    fun onToggleFailClosed(failClosed: Boolean) {
        viewModelScope.launch {
            studyModeRepository.setFailClosed(failClosed)
        }
    }

    fun onToggleChannel(channelId: String, isAllowlisted: Boolean) {
        viewModelScope.launch {
            studyModeRepository.toggleChannelAllowlist(channelId, isAllowlisted)
        }
    }

    fun onAddStudyChannel(channelTitle: String) {
        viewModelScope.launch {
            studyModeRepository.addChannel(
                StudyChannel(
                    channelId = "custom_${UUID.randomUUID()}",
                    channelTitle = channelTitle,
                    category = "Custom",
                    isCuratedDefault = false,
                    isAllowlisted = true,
                )
            )
            userMessage.value = "Added '$channelTitle' to Study Mode allowlist."
        }
    }

    fun onClearMessage() {
        userMessage.value = null
    }
}
