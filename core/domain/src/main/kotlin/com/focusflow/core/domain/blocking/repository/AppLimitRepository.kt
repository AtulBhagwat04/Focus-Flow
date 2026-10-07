package com.focusflow.core.domain.blocking.repository

import com.focusflow.core.domain.blocking.model.DailyAppLimit
import kotlinx.coroutines.flow.Flow

/**
 * Access to configured daily time and launch count limits.
 */
interface AppLimitRepository {
    fun observeAllLimits(): Flow<List<DailyAppLimit>>
    fun observeLimitForPackage(packageName: String): Flow<DailyAppLimit?>
    suspend fun setLimit(limit: DailyAppLimit)
    suspend fun removeLimit(packageName: String)
}
