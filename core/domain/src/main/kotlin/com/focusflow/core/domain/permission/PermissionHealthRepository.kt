package com.focusflow.core.domain.permission

import kotlinx.coroutines.flow.Flow

/**
 * Repository providing observable access to system permission statuses.
 */
interface PermissionHealthRepository {
    /**
     * Observes real-time permission health.
     */
    val permissionHealth: Flow<PermissionHealth>

    /**
     * Triggers an immediate refresh of current permission states.
     */
    suspend fun refreshHealth()
}
