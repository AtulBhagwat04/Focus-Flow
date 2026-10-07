package com.focusflow.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionHealthRepository
import com.focusflow.core.domain.usage.model.DailyUsageStats
import com.focusflow.core.domain.usage.repository.UsageStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val TOP_APPS_LIMIT = 5

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val permissionHealthRepository: PermissionHealthRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(StatsTimeRange.DAY)
    private val isRefreshing = MutableStateFlow(false)

    private val todayEpochDay: Long
        get() {
            val instant = Instant.ofEpochMilli(clock.now())
            return LocalDate.ofInstant(instant, ZoneId.systemDefault()).toEpochDay()
        }

    val uiState: StateFlow<StatsUiState> = combine(
        selectedRange,
        usageStatsRepository.observeDailyUsage(todayEpochDay),
        permissionHealthRepository.permissionHealth,
        isRefreshing,
    ) { range, dailyStats, health, refreshing ->
        val hasUsage = health.stateOf(AppPermission.USAGE_ACCESS).isGranted
        buildUiState(
            range = range,
            stats = dailyStats,
            hasPermission = hasUsage,
            isLoading = refreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StatsUiState(isLoading = true),
    )

    init {
        refresh()
    }

    fun onSelectRange(range: StatsTimeRange) {
        selectedRange.value = range
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                permissionHealthRepository.refreshHealth()
                usageStatsRepository.refreshUsageForDay(todayEpochDay)
            } finally {
                isRefreshing.value = false
            }
        }
    }

    private fun buildUiState(
        range: StatsTimeRange,
        stats: DailyUsageStats,
        hasPermission: Boolean,
        isLoading: Boolean,
    ): StatsUiState {
        val totalMs = stats.totalScreenTimeMs
        val topList = stats.topApps.take(TOP_APPS_LIMIT).map { summary ->
            val fraction = if (totalMs > 0L) {
                (summary.totalTimeForegroundMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            AppUsageItem(
                packageName = summary.packageName,
                appName = summary.appName,
                formattedDuration = formatDuration(summary.totalTimeForegroundMs),
                launchCount = summary.launchCount,
                fractionOfTotal = fraction,
                isSafeListed = summary.isSafeListed,
            )
        }

        return StatsUiState(
            selectedRange = range,
            totalScreenTimeFormatted = formatDuration(stats.totalScreenTimeMs),
            totalFocusTimeFormatted = formatDuration(stats.totalFocusTimeMs),
            focusRatio = stats.focusRatio,
            unlockCount = stats.unlockCount,
            topApps = topList,
            hasUsagePermission = hasPermission,
            isLoading = isLoading,
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val totalMinutes = durationMs / MILLIS_PER_MINUTE
        val hours = totalMinutes / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}
