package com.focusflow.core.domain.blocking.repository

import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.RemoteDetectionSelectors
import kotlinx.coroutines.flow.Flow

interface AdvancedBlockingRepository {
    fun getConfig(): Flow<AdvancedBlockingConfig>
    suspend fun setShortsBlockingEnabled(enabled: Boolean): Result<Unit>
    suspend fun setReelsBlockingEnabled(enabled: Boolean): Result<Unit>
    suspend fun setBlockDuringFocusOnly(focusOnly: Boolean): Result<Unit>
    fun getRemoteSelectors(): Flow<RemoteDetectionSelectors>
}
