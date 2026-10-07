package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.FriendEntity
import com.focusflow.core.data.local.entity.LeaderboardEntryEntity
import com.focusflow.core.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {
    @Query("SELECT * FROM focus_rooms ORDER BY createdAtTimestamp DESC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM focus_rooms WHERE id = :roomId LIMIT 1")
    fun getRoomById(roomId: String): Flow<RoomEntity?>

    @Query("SELECT * FROM focus_rooms WHERE isJoined = 1 LIMIT 1")
    fun getActiveJoinedRoom(): Flow<RoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Query("UPDATE focus_rooms SET isJoined = :isJoined WHERE id = :roomId")
    suspend fun setRoomJoined(roomId: String, isJoined: Boolean)

    @Query("UPDATE focus_rooms SET isJoined = 0")
    suspend fun leaveAllRooms()

    @Query("DELETE FROM focus_rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Query("SELECT * FROM friends")
    fun getFriends(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteFriend(userId: String)

    @Query("SELECT * FROM leaderboard_entries WHERE scope = :scope ORDER BY rank ASC")
    fun getLeaderboard(scope: String): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntryEntity>)

    @Query("DELETE FROM leaderboard_entries WHERE scope = :scope")
    suspend fun clearLeaderboard(scope: String)
}
