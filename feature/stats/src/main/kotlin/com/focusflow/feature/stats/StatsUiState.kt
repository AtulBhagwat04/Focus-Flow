package com.focusflow.feature.stats

import com.focusflow.core.domain.usage.model.AppUsageSummary

/**
 * Filter intervals for the Stats dashboard.
 */
enum class StatsTimeRange {
    DAY,
    WEEK,
    MONTH,
}

/**
 * Presentation model for an app in the top usage list.
 */
data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val formattedDuration: String,
    val launchCount: Int,
    val fractionOfTotal: Float,
    val isSafeListed: Boolean,
)

/**
 * Immutable UI state for the Stats screen.
 */
data class StatsUiState(
    val selectedRange: StatsTimeRange = StatsTimeRange.DAY,
    val totalScreenTimeFormatted: String = "0m",
    val totalFocusTimeFormatted: String = "0m",
    val focusRatio: Float = 0f,
    val unlockCount: Int = 0,
    val topApps: List<AppUsageItem> = emptyList(),
    val dailyHours: List<Float> = emptyList(),
    val hasUsagePermission: Boolean = true,
    val isLoading: Boolean = false,
)
