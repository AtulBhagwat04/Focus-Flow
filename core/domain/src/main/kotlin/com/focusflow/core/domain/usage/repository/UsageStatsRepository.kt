package com.focusflow.core.domain.usage.repository

import com.focusflow.core.domain.usage.model.DailyUsageStats
import kotlinx.coroutines.flow.Flow

/**
 * Access to historical and today's aggregated usage metrics.
 */
interface UsageStatsRepository {
    /**
     * Observes aggregated usage for a specific epoch day.
     */
    fun observeDailyUsage(epochDay: Long): Flow<DailyUsageStats>

    /**
     * Observes daily summaries over a range of days [startEpochDay, endEpochDay] (inclusive).
     */
    fun observeDateRangeUsage(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyUsageStats>>

    /**
     * Triggers a sync/re-calculation of usage stats from system usage events for the given day.
     */
    suspend fun refreshUsageForDay(epochDay: Long)
}
