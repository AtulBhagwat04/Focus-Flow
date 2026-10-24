package com.focusflow.feature.home.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val tasks: StateFlow<List<FocusTask>> = taskRepository.tasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val activeTask: StateFlow<FocusTask?> = taskRepository.activeTask
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun onToggleTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(taskId)
        }
    }

    fun onStartFocus(taskId: String, onNavigate: (String, Int) -> Unit) {
        viewModelScope.launch {
            val task = taskRepository.selectTaskForFocus(taskId)
            if (task != null) {
                onNavigate(task.title, task.durationMinutes)
            }
        }
    }

    fun onAddTask(title: String, durationMinutes: Int) {
        viewModelScope.launch {
            taskRepository.addTask(title, durationMinutes)
        }
    }

    fun onUpdateTask(taskId: String, title: String, durationMinutes: Int) {
        viewModelScope.launch {
            taskRepository.updateTask(taskId, title, durationMinutes)
        }
    }

    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
}
