package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.StudyChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyModeDao {
    @Query("SELECT * FROM study_channels ORDER BY channelTitle ASC")
    fun getAllChannels(): Flow<List<StudyChannelEntity>>

    @Query("SELECT * FROM study_channels WHERE isAllowlisted = 1")
    fun getAllowlistedChannels(): Flow<List<StudyChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<StudyChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: StudyChannelEntity)

    @Query("UPDATE study_channels SET isAllowlisted = :isAllowlisted WHERE channelId = :channelId")
    suspend fun setAllowlisted(channelId: String, isAllowlisted: Boolean)

    @Query("DELETE FROM study_channels WHERE channelId = :channelId")
    suspend fun deleteChannel(channelId: String)

    @Query("SELECT COUNT(*) FROM study_channels")
    suspend fun count(): Int
}
