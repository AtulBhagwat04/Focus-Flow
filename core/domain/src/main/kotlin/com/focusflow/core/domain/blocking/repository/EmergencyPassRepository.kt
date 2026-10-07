package com.focusflow.core.domain.blocking.repository

import com.focusflow.core.domain.blocking.model.EmergencyPass
import kotlinx.coroutines.flow.Flow

/**
 * Access to emergency bypass pass state.
 */
interface EmergencyPassRepository {
    val emergencyPass: Flow<EmergencyPass>
    suspend fun requestPass(durationMs: Long = 5 * 60 * 1000L): Boolean
    suspend fun resetDailyPasses()
}
