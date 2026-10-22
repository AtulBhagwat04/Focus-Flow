package com.focusflow.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.model.SubjectTag
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.SubjectTagRepository
import com.focusflow.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository,
    private val subjectTagRepository: SubjectTagRepository,
    private val ambientAudioPlayer: AmbientAudioPlayer,
    private val clock: Clock,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        observeData()
    }

    private data class ObservedTimerData(
        val session: FocusSession?,
        val tags: List<SubjectTag>,
        val sound: AmbientSound,
        val task: FocusTask?,
    )

    private fun observeData() {
        combine(
            focusSessionRepository.currentSession,
            subjectTagRepository.getAllTags(),
            ambientAudioPlayer.currentSound,
            taskRepository.activeTask,
        ) { session, tags, sound, task ->
            ObservedTimerData(session, tags, sound, task)
        }.onEach { (session, tags, sound, activeTask) ->
            _uiState.update { current ->
                val now = clock.now()
                val selectedTag = current.selectedTag ?: tags.firstOrNull()

                // If a task is active and session is not yet active, auto-manage focus duration
                val mode = when {
                    session != null -> session.mode
                    activeTask != null && !current.isActive -> {
                        val durationMs = activeTask.durationMinutes * 60 * 1000L
                        if (activeTask.durationMinutes == 25) {
                            SessionMode.Pomodoro(durationMs)
                        } else {
                            SessionMode.Custom(durationMs)
                        }
                    }
                    else -> current.selectedMode
                }

                val targetDuration = session?.targetDurationMs
                    ?: when (mode) {
                        is SessionMode.Pomodoro -> mode.workDurationMs
                        is SessionMode.Custom -> mode.durationMs
                        else -> DEFAULT_COUNTDOWN_MS
                    }

                val isRunning = session?.state == SessionState.RUNNING
                val isPaused = session?.state == SessionState.PAUSED
                val elapsed = session?.elapsedDurationMs(now) ?: 0L
                val remaining = session?.remainingDurationMs(now) ?: targetDuration
                val progress = session?.progressFraction(now) ?: 0f

                current.copy(
                    session = session,
                    activeTask = activeTask,
                    availableTags = tags,
                    selectedTag = selectedTag,
                    selectedSound = sound,
                    selectedMode = mode,
                    elapsedMs = elapsed,
                    remainingMs = remaining,
                    progress = progress,
                    isRunning = isRunning,
                    isPaused = isPaused,
                )
            }

            if (session?.state == SessionState.RUNNING) {
                if (tickerJob?.isActive != true) {
                    startTicker(session)
                }
            } else {
                stopTicker()
            }
        }.launchIn(viewModelScope)
    }

    private fun startTicker(session: FocusSession) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                val now = clock.now()
                val elapsed = session.elapsedDurationMs(now)
                val remaining = session.remainingDurationMs(now)
                val progress = session.progressFraction(now)

                _uiState.update { current ->
                    current.copy(
                        elapsedMs = elapsed,
                        remainingMs = remaining,
                        progress = progress,
                    )
                }

                // Record progress to active task every second
                val currentTask = _uiState.value.activeTask
                if (currentTask != null) {
                    taskRepository.recordFocusProgress(currentTask.id, 1)
                }

                if (session.isTargetReached(now)) {
                    onCompleteClicked()
                    break
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun onStartClicked() {
        viewModelScope.launch {
            val state = _uiState.value
            val started = focusSessionRepository.startSession(
                mode = state.selectedMode,
                tagId = state.selectedTag?.id,
            )
            if (state.selectedSound != AmbientSound.NONE) {
                ambientAudioPlayer.play(state.selectedSound)
            }
            _uiState.update { it.copy(session = started) }
        }
    }

    fun onPauseClicked() {
        viewModelScope.launch {
            focusSessionRepository.pauseSession()
            ambientAudioPlayer.pause()
        }
    }

    fun onResumeClicked() {
        viewModelScope.launch {
            focusSessionRepository.resumeSession()
            if (_uiState.value.selectedSound != AmbientSound.NONE) {
                ambientAudioPlayer.resume()
            }
        }
    }

    fun onCompleteClicked() {
        viewModelScope.launch {
            val activeTaskId = _uiState.value.activeTask?.id
            if (activeTaskId != null) {
                taskRepository.completeTask(activeTaskId)
            }
            val completed = focusSessionRepository.completeSession()
            ambientAudioPlayer.stop()
            stopTicker()

            if (completed != null) {
                _uiState.update {
                    it.copy(
                        showCelebrationDialog = true,
                        completedXpEarned = completed.xpEarned,
                    )
                }
            }
        }
    }

    fun onClearActiveTask() {
        viewModelScope.launch {
            taskRepository.clearActiveTask()
        }
    }

    fun onAbortClicked() {
        viewModelScope.launch {
            focusSessionRepository.abortSession()
            ambientAudioPlayer.stop()
            stopTicker()
        }
    }

    fun onSelectDurationMinutes(minutes: Int) {
        if (_uiState.value.isActive) return
        val durationMs = minutes * 60 * 1000L
        val mode = if (minutes == 25) {
            SessionMode.Pomodoro(durationMs)
        } else {
            SessionMode.Custom(durationMs)
        }
        onSelectMode(mode)
    }

    fun onToggleBlockApps(enabled: Boolean) {
        _uiState.update { it.copy(isBlockAppsEnabled = enabled) }
    }

    fun onSelectMode(mode: SessionMode) {
        if (_uiState.value.isActive) return
        val defaultDuration = when (mode) {
            is SessionMode.Pomodoro -> mode.workDurationMs
            is SessionMode.Custom -> mode.durationMs
            is SessionMode.UntilTime -> (mode.targetEpochMs - clock.now()).coerceAtLeast(0L)
            is SessionMode.Stopwatch -> 0L
        }
        _uiState.update {
            it.copy(
                selectedMode = mode,
                remainingMs = defaultDuration,
                elapsedMs = 0L,
                progress = 0f,
            )
        }
    }

    fun onSelectTag(tag: SubjectTag) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun onSelectSound(sound: AmbientSound) {
        _uiState.update { it.copy(selectedSound = sound) }
        if (_uiState.value.isRunning) {
            ambientAudioPlayer.play(sound)
        }
    }

    fun onDismissCelebration() {
        _uiState.update { it.copy(showCelebrationDialog = false) }
    }

    companion object {
        private const val TICK_INTERVAL_MS = 1000L
        private const val DEFAULT_COUNTDOWN_MS = 25 * 60 * 1000L
    }
}
