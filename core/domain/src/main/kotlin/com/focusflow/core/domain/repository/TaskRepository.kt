package com.focusflow.core.domain.repository

import com.focusflow.core.domain.model.FocusTask
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for focus tasks.
 *
 * Allows scheduling tasks with dedicated focus durations,
 * selecting a task to focus on, tracking completed focus time,
 * and marking tasks completed.
 */
interface TaskRepository {

    /**
     * Observable list of all focus tasks.
     */
    val tasks: Flow<List<FocusTask>>

    /**
     * Currently active focus task targeted by the timer, or null if general focus.
     */
    val activeTask: Flow<FocusTask?>

    suspend fun getTasks(): List<FocusTask>

    suspend fun addTask(title: String, durationMinutes: Int): FocusTask

    suspend fun toggleTaskCompleted(taskId: String): FocusTask?

    /**
     * Sets this task as the active focus task and notifies listeners.
     */
    suspend fun selectTaskForFocus(taskId: String): FocusTask?

    /**
     * Adds focus seconds to a task and updates its progress.
     */
    suspend fun recordFocusProgress(taskId: String, additionalSeconds: Int): FocusTask?

    /**
     * Marks the task as completed upon finishing a focus session.
     */
    suspend fun completeTask(taskId: String): FocusTask?

    /**
     * Clears the currently active task.
     */
    suspend fun clearActiveTask()

    /**
     * Updates an existing task's title and focus duration.
     */
    suspend fun updateTask(taskId: String, title: String, durationMinutes: Int): FocusTask? = null

    /**
     * Deletes a task by ID.
     */
    suspend fun deleteTask(taskId: String) = Unit
}
