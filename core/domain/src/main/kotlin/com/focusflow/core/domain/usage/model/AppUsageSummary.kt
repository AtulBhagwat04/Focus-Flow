package com.focusflow.core.domain.usage.model

/**
 * Summary of usage for a single application across a given time interval.
 */
data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val totalTimeForegroundMs: Long,
    val launchCount: Int,
    val lastTimeUsedEpochMs: Long,
    val isSafeListed: Boolean,
)
