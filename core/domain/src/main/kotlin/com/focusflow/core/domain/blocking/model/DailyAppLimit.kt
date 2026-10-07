package com.focusflow.core.domain.blocking.model

/**
 * Daily usage limits configured for a package.
 */
data class DailyAppLimit(
    val packageName: String,
    val dailyTimeLimitMs: Long? = null,
    val dailyLaunchLimit: Int? = null,
) {
    val hasActiveLimits: Boolean
        get() = (dailyTimeLimitMs != null && dailyTimeLimitMs > 0L) ||
            (dailyLaunchLimit != null && dailyLaunchLimit > 0)
}
