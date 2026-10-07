package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.AppUsageEntity
import com.focusflow.core.data.local.entity.DailyUsageEntity
import com.focusflow.core.data.local.entity.UserEssentialAppEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for device usage metrics and user-designated essential apps.
 */
@Dao
interface UsageDao {

    @Query("SELECT * FROM daily_usage WHERE epochDay = :epochDay LIMIT 1")
    fun observeDailyUsage(epochDay: Long): Flow<DailyUsageEntity?>

    @Query("SELECT * FROM daily_usage WHERE epochDay >= :startEpochDay AND epochDay <= :endEpochDay ORDER BY epochDay ASC")
    fun observeDateRangeUsage(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUsage(entity: DailyUsageEntity)

    @Query("SELECT * FROM app_usage WHERE epochDay = :epochDay ORDER BY totalTimeForegroundMs DESC")
    fun observeAppUsagesForDay(epochDay: Long): Flow<List<AppUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsages(entities: List<AppUsageEntity>)

    @Query("SELECT * FROM user_essential_apps")
    fun observeUserEssentials(): Flow<List<UserEssentialAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserEssential(entity: UserEssentialAppEntity)

    @Query("DELETE FROM user_essential_apps WHERE packageName = :packageName")
    suspend fun deleteUserEssential(packageName: String)
}
