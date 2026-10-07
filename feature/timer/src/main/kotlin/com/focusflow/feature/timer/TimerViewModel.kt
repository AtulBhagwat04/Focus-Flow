package com.focusflow.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.model.SubjectTag
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.SubjectTagRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            focusSessionRepository.currentSession,
            subjectTagRepository.getAllTags(),
            ambientAudioPlayer.currentSound,
        ) { session, tags, sound ->
            Triple(session, tags, sound)
        }.onEach { (session, tags, sound) ->
            _uiState.update { current ->
                val now = clock.now()
                val selectedTag = current.selectedTag ?: tags.firstOrNull()
                val targetDuration = session?.targetDurationMs
                    ?: (current.selectedMode as? SessionMode.Pomodoro)?.workDurationMs
                    ?: DEFAULT_COUNTDOWN_MS

                val isRunning = session?.state == SessionState.RUNNING
                val isPaused = session?.state == SessionState.PAUSED
                val elapsed = session?.elapsedDurationMs(now) ?: 0L
                val remaining = session?.remainingDurationMs(now) ?: targetDuration
                val progress = session?.progressFraction(now) ?: 0f

                current.copy(
                    session = session,
                    availableTags = tags,
                    selectedTag = selectedTag,
                    selectedSound = sound,
                    selectedMode = session?.mode ?: current.selectedMode,
                    elapsedMs = elapsed,
                    remainingMs = remaining,
                    progress = progress,
                    isRunning = isRunning,
                    isPaused = isPaused,
                )
            }

            if (session?.state == SessionState.RUNNING) {
                startTicker(session)
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

    fun onAbortClicked() {
        viewModelScope.launch {
            focusSessionRepository.abortSession()
            ambientAudioPlayer.stop()
            stopTicker()
        }
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
