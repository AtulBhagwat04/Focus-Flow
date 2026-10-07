package com.focusflow.core.domain.sync.repository

import com.focusflow.core.domain.sync.model.SyncResult
import kotlinx.coroutines.flow.Flow

/**
 * Access to offline-then-online data sync and portability export.
 */
interface SyncRepository {
    val isSyncing: Flow<Boolean>

    /**
     * Enqueues a background sync task via WorkManager with network constraints.
     */
    fun enqueueSync()

    /**
     * Executes immediate cloud synchronization of local focus sessions and profile metrics.
     */
    suspend fun performSync(): SyncResult

    /**
     * Generates a complete, portable JSON document representing the user's focus sessions and preferences.
     */
    suspend fun exportUserDataJson(): Result<String>
}
