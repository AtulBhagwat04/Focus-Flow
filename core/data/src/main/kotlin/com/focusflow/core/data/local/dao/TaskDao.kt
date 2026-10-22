package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.focusflow.core.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAtEpochMs DESC")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCurrentFocus = 1 LIMIT 1")
    fun getActiveTask(): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY createdAtEpochMs DESC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET isCurrentFocus = 0")
    suspend fun clearActiveTask()

    @Query("UPDATE tasks SET isCurrentFocus = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setActiveTask(id: String)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
