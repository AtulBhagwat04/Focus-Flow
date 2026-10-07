package com.focusflow.core.domain.usage.model

/**
 * Daily aggregated usage and engagement metrics for the device.
 */
data class DailyUsageStats(
    val epochDay: Long,
    val totalScreenTimeMs: Long,
    val totalFocusTimeMs: Long,
    val unlockCount: Int,
    val topApps: List<AppUsageSummary>,
) {
    /**
     * Ratio of productive focus time to total screen time (0.0f to 1.0f).
     */
    val focusRatio: Float
        get() = if (totalScreenTimeMs > 0L) {
            (totalFocusTimeMs.toFloat() / totalScreenTimeMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}
