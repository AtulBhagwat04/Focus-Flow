package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.AppLimitEntity
import com.focusflow.core.data.local.entity.BlockRuleEntity
import com.focusflow.core.data.local.entity.BlockScheduleEntity
import com.focusflow.core.data.local.entity.EmergencyPassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockingDao {

    // --- Block Rules ---
    @Query("SELECT * FROM block_rules")
    fun observeAllRules(): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rules WHERE packageName = :packageName LIMIT 1")
    fun observeRuleForPackage(packageName: String): Flow<BlockRuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: BlockRuleEntity)

    @Query("DELETE FROM block_rules WHERE packageName = :packageName")
    suspend fun deleteRule(packageName: String)

    // --- App Limits ---
    @Query("SELECT * FROM app_limits")
    fun observeAllLimits(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1")
    fun observeLimitForPackage(packageName: String): Flow<AppLimitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLimit(limit: AppLimitEntity)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteLimit(packageName: String)

    // --- Block Schedules ---
    @Query("SELECT * FROM block_schedules")
    fun observeAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: BlockScheduleEntity)

    @Query("DELETE FROM block_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)

    // --- Emergency Pass ---
    @Query("SELECT * FROM emergency_pass_state WHERE id = 1 LIMIT 1")
    fun observeEmergencyPass(): Flow<EmergencyPassEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmergencyPass(entity: EmergencyPassEntity)
}
