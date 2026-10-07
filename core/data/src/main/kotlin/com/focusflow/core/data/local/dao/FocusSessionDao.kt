package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query(
        """
        SELECT * FROM focus_sessions 
        WHERE state IN ('RUNNING', 'PAUSED') 
        ORDER BY startedAtEpochMs DESC 
        LIMIT 1
        """
    )
    fun getActiveSession(): Flow<FocusSessionEntity?>

    @Query(
        """
        SELECT * FROM focus_sessions 
        WHERE state IN ('COMPLETED', 'ABORTED') 
        ORDER BY startedAtEpochMs DESC 
        LIMIT :limit
        """
    )
    fun getHistoricalSessions(limit: Int): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): FocusSessionEntity?

    @Query(
        """
        SELECT COALESCE(SUM(completedAtEpochMs - startedAtEpochMs - totalPausedDurationMs), 0)
        FROM focus_sessions
        WHERE state = 'COMPLETED'
          AND startedAtEpochMs >= :startEpochMs
          AND startedAtEpochMs < :endEpochMs
        """
    )
    fun observeFocusDurationBetween(startEpochMs: Long, endEpochMs: Long): Flow<Long>

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}
