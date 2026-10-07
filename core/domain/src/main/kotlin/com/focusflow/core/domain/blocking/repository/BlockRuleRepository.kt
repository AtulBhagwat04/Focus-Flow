package com.focusflow.core.domain.blocking.repository

import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import kotlinx.coroutines.flow.Flow

/**
 * Access to configured blocking rules and recurring schedules.
 */
interface BlockRuleRepository {
    fun observeAllRules(): Flow<List<AppBlockRule>>
    fun observeRuleForPackage(packageName: String): Flow<AppBlockRule?>
    suspend fun setRule(rule: AppBlockRule)
    suspend fun removeRule(packageName: String)

    fun observeAllSchedules(): Flow<List<BlockSchedule>>
    suspend fun upsertSchedule(schedule: BlockSchedule)
    suspend fun deleteSchedule(scheduleId: String)
}
