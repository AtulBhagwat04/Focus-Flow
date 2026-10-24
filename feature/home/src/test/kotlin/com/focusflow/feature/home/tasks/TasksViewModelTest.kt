package com.focusflow.feature.home.tasks

import com.focusflow.core.domain.model.FocusTask
import com.focusflow.core.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeTaskRepository : TaskRepository {
        val tasksFlow = MutableStateFlow<List<FocusTask>>(emptyList())
        val activeTaskFlow = MutableStateFlow<FocusTask?>(null)

        override val tasks: Flow<List<FocusTask>> = tasksFlow
        override val activeTask: Flow<FocusTask?> = activeTaskFlow

        override suspend fun getTasks(): List<FocusTask> = tasksFlow.value

        override suspend fun addTask(title: String, durationMinutes: Int): FocusTask {
            val task = FocusTask(
                id = "task-${tasksFlow.value.size + 1}",
                title = title,
                durationMinutes = durationMinutes,
            )
            tasksFlow.value = tasksFlow.value + task
            return task
        }

        override suspend fun toggleTaskCompleted(taskId: String): FocusTask? {
            var updatedTask: FocusTask? = null
            tasksFlow.value = tasksFlow.value.map { task ->
                if (task.id == taskId) {
                    val toggled = task.copy(isCompleted = !task.isCompleted)
                    updatedTask = toggled
                    toggled
                } else task
            }
            return updatedTask
        }

        override suspend fun selectTaskForFocus(taskId: String): FocusTask? {
            val target = tasksFlow.value.find { it.id == taskId } ?: return null
            val active = target.copy(isCurrentFocus = true)
            activeTaskFlow.value = active
            return active
        }

        override suspend fun recordFocusProgress(taskId: String, additionalSeconds: Int): FocusTask? {
            var updatedTask: FocusTask? = null
            tasksFlow.value = tasksFlow.value.map { task ->
                if (task.id == taskId) {
                    val updated = task.copy(
                        completedSeconds = task.completedSeconds + additionalSeconds,
                    )
                    updatedTask = updated
                    updated
                } else task
            }
            return updatedTask
        }

        override suspend fun completeTask(taskId: String): FocusTask? {
            var updatedTask: FocusTask? = null
            tasksFlow.value = tasksFlow.value.map { task ->
                if (task.id == taskId) {
                    val updated = task.copy(isCompleted = true, isCurrentFocus = false)
                    updatedTask = updated
                    updated
                } else task
            }
            return updatedTask
        }

        override suspend fun clearActiveTask() {
            activeTaskFlow.value = null
        }

        override suspend fun updateTask(taskId: String, title: String, durationMinutes: Int): FocusTask? {
            var updatedTask: FocusTask? = null
            tasksFlow.value = tasksFlow.value.map { task ->
                if (task.id == taskId) {
                    val updated = task.copy(title = title, durationMinutes = durationMinutes)
                    updatedTask = updated
                    updated
                } else task
            }
            return updatedTask
        }

        override suspend fun deleteTask(taskId: String) {
            tasksFlow.value = tasksFlow.value.filterNot { it.id == taskId }
            if (activeTaskFlow.value?.id == taskId) {
                activeTaskFlow.value = null
            }
        }
    }

    private lateinit var taskRepository: FakeTaskRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = FakeTaskRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TasksViewModel {
        return TasksViewModel(taskRepository)
    }

    @Test
    fun onAddTask_addsTaskToRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddTask("Deep Work Session", 50)

        val currentTasks = taskRepository.tasksFlow.value
        assertEquals(1, currentTasks.size)
        assertEquals("Deep Work Session", currentTasks[0].title)
        assertEquals(50, currentTasks[0].durationMinutes)
    }

    @Test
    fun onUpdateTask_modifiesExistingTaskInRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddTask("Drafting report", 25)
        val taskId = taskRepository.tasksFlow.value.first().id

        viewModel.onUpdateTask(taskId, "Drafting updated report", 45)

        val updatedTask = taskRepository.tasksFlow.value.first()
        assertEquals("Drafting updated report", updatedTask.title)
        assertEquals(45, updatedTask.durationMinutes)
    }

    @Test
    fun onDeleteTask_removesTaskFromRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddTask("Task to be deleted", 25)
        val taskId = taskRepository.tasksFlow.value.first().id

        viewModel.onDeleteTask(taskId)

        assertTrue(taskRepository.tasksFlow.value.isEmpty())
    }

    @Test
    fun onToggleTask_togglesCompletionStatus() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddTask("Read Chapter 5", 30)
        val taskId = taskRepository.tasksFlow.value.first().id

        viewModel.onToggleTask(taskId)
        assertTrue(taskRepository.tasksFlow.value.first().isCompleted)

        viewModel.onToggleTask(taskId)
        assertTrue(!taskRepository.tasksFlow.value.first().isCompleted)
    }

    @Test
    fun onStartFocus_selectsTaskAndInvokesCallback() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddTask("Chemistry Assignment", 45)
        val taskId = taskRepository.tasksFlow.value.first().id

        var navigatedTitle: String? = null
        var navigatedDuration: Int? = null

        viewModel.onStartFocus(taskId) { title, duration ->
            navigatedTitle = title
            navigatedDuration = duration
        }

        assertEquals("Chemistry Assignment", navigatedTitle)
        assertEquals(45, navigatedDuration)
        assertNotNull(taskRepository.activeTaskFlow.value)
        assertEquals(taskId, taskRepository.activeTaskFlow.value?.id)
    }
}
