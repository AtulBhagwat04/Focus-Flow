package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusflow.core.domain.model.FocusTask

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val durationMinutes: Int,
    val completedSeconds: Int,
    val isCompleted: Boolean,
    val isCurrentFocus: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
)

fun TaskEntity.asDomainModel(): FocusTask = FocusTask(
    id = id,
    title = title,
    durationMinutes = durationMinutes,
    completedSeconds = completedSeconds,
    isCompleted = isCompleted,
    isCurrentFocus = isCurrentFocus,
)

fun FocusTask.asEntity(
    createdAtEpochMs: Long = System.currentTimeMillis(),
    updatedAtEpochMs: Long = System.currentTimeMillis(),
): TaskEntity = TaskEntity(
    id = id,
    title = title,
    durationMinutes = durationMinutes,
    completedSeconds = completedSeconds,
    isCompleted = isCompleted,
    isCurrentFocus = isCurrentFocus,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
)
