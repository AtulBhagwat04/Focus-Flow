package com.focusflow.core.domain.blocking.repository

import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import kotlinx.coroutines.flow.Flow

interface StudyModeRepository {
    fun getStudyModeConfig(): Flow<StudyModeConfig>
    suspend fun setStudyModeEnabled(enabled: Boolean): Result<Unit>
    suspend fun setFailClosed(failClosed: Boolean): Result<Unit>
    fun getAllowlistedChannels(): Flow<List<StudyChannel>>
    suspend fun addChannel(channel: StudyChannel): Result<Unit>
    suspend fun removeChannel(channelId: String): Result<Unit>
    suspend fun toggleChannelAllowlist(channelId: String, isAllowlisted: Boolean): Result<Unit>
}
