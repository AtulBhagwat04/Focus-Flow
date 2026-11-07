package com.focusflow.feature.stats

/**
 * Filter intervals for the Stats dashboard.
 */
enum class StatsTimeRange {
    DAY,
    WEEK,
    MONTH,
}

/**
 * Presentation model for a bar in the focus breakdown chart.
 */
data class ChartBarItem(
    val id: String,
    val label: String,
    val fullLabel: String,
    val hours: Float,
    val formattedDuration: String,
    val isGoalReached: Boolean,
    val isCurrent: Boolean = false,
)

/**
 * Presentation model for the 2x2 Highlights card grid.
 */
data class StatsHighlights(
    val bestDayName: String = "—",
    val bestDayCaption: String = "—",
    val bestTimeWindow: String = "—",
    val bestTimeCaption: String = "Best time to focus",
    val avgSessionLength: String = "—",
    val avgSessionCaption: String = "Avg session length",
    val streakCount: String = "—",
    val streakCaption: String = "Goal days met",
)

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
    val selectedRange: StatsTimeRange = StatsTimeRange.WEEK,
    val periodOffset: Int = 0,
    val dateRangeLabel: String = "",
    val canNavigateNext: Boolean = false,
    val focusHeroLabel: String = "Focused this week",
    val totalFocusTimeFormatted: String = "0m",
    val subtitleText: String = "How you're doing this week",
    val trendText: String = "Start a session to build your focus momentum.",
    val isTrendPositive: Boolean = true,
    val goalLabel: String = "Weekly goal: 16h",
    val goalRemainingText: String = "16h to go",
    val goalProgress: Float = 0f,
    val chartTitle: String = "Focus each day",
    val chartBars: List<ChartBarItem> = emptyList(),
    val dailyGoalHours: Float = 2.33f,
    val dailyGoalFormatted: String = "2h 20m",
    val highlights: StatsHighlights = StatsHighlights(),
    val totalScreenTimeFormatted: String = "0m",
    val focusRatio: Float = 0f,
    val unlockCount: Int = 0,
    val topApps: List<AppUsageItem> = emptyList(),
    val dailyHours: List<Float> = emptyList(),
    val hasUsagePermission: Boolean = true,
    val isLoading: Boolean = false,
)
