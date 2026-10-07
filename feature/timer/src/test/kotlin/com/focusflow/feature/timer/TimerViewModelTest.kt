package com.focusflow.feature.timer

import com.focusflow.core.common.time.Clock
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.model.SubjectTag
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.SubjectTagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeFocusSessionRepository : FocusSessionRepository {
        val sessionFlow = MutableStateFlow<FocusSession?>(null)
        override val currentSession: Flow<FocusSession?> = sessionFlow

        override suspend fun startSession(
            mode: SessionMode,
            tagId: String?,
            customTargetDurationMs: Long?,
        ): FocusSession {
            val session = FocusSession(
                id = "test-session",
                mode = mode,
                state = SessionState.RUNNING,
                tagId = tagId,
                targetDurationMs = 25 * 60 * 1000L,
                startedAtEpochMs = 1_000_000L,
            )
            sessionFlow.value = session
            return session
        }

        override suspend fun pauseSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val paused = current.copy(state = SessionState.PAUSED, pausedAtEpochMs = 1_300_000L)
            sessionFlow.value = paused
            return paused
        }

        override suspend fun resumeSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val resumed = current.copy(state = SessionState.RUNNING, pausedAtEpochMs = null)
            sessionFlow.value = resumed
            return resumed
        }

        override suspend fun completeSession(): FocusSession? {
            val current = sessionFlow.value ?: return null
            val completed = current.copy(
                state = SessionState.COMPLETED,
                completedAtEpochMs = 2_500_000L,
                xpEarned = 25,
            )
            sessionFlow.value = null
            return completed
        }

        override suspend fun abortSession(): FocusSession? {
            sessionFlow.value = null
            return null
        }

        override fun getSessionHistory(limit: Int): Flow<List<FocusSession>> = flowOf(emptyList())
    }

    private class FakeSubjectTagRepository : SubjectTagRepository {
        val tags = listOf(
            SubjectTag("1", "Deep Work", "#3D6BCC", "work", true),
            SubjectTag("2", "Study", "#2E8B7A", "school", false),
        )

        override fun getAllTags(): Flow<List<SubjectTag>> = flowOf(tags)
        override suspend fun saveTag(tag: SubjectTag) {}
        override suspend fun deleteTag(tagId: String) {}
    }

    private class FakeAudioPlayer : AmbientAudioPlayer {
        val soundState = MutableStateFlow(AmbientSound.NONE)
        val playingState = MutableStateFlow(false)

        override val currentSound: StateFlow<AmbientSound> = soundState.asStateFlow()
        override val isPlaying: StateFlow<Boolean> = playingState.asStateFlow()

        override fun play(sound: AmbientSound) {
            soundState.value = sound
            playingState.value = true
        }

        override fun pause() { playingState.value = false }
        override fun resume() { playingState.value = true }
        override fun stop() {
            soundState.value = AmbientSound.NONE
            playingState.value = false
        }
        override fun release() { stop() }
    }

    private class FakeClock : Clock {
        override fun now(): Long = 1_000_000L
    }

    private lateinit var sessionRepository: FakeFocusSessionRepository
    private lateinit var tagRepository: FakeSubjectTagRepository
    private lateinit var audioPlayer: FakeAudioPlayer
    private lateinit var clock: FakeClock
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sessionRepository = FakeFocusSessionRepository()
        tagRepository = FakeSubjectTagRepository()
        audioPlayer = FakeAudioPlayer()
        clock = FakeClock()
        viewModel = TimerViewModel(sessionRepository, tagRepository, audioPlayer, clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsAvailableTags_andDefaultDuration() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(2, state.availableTags.size)
        assertEquals("Deep Work", state.selectedTag?.name)
        assertFalse(state.isActive)
        assertEquals(25 * 60 * 1000L, state.remainingMs)
    }

    @Test
    fun startSession_updatesRunningState_andTriggersAudioIfSelected() = runTest {
        advanceUntilIdle()
        viewModel.onSelectSound(AmbientSound.RAIN)
        viewModel.onStartClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertTrue(state.isActive)
        assertTrue(audioPlayer.playingState.value)
        assertEquals(AmbientSound.RAIN, audioPlayer.soundState.value)
    }

    @Test
    fun completeSession_stopsAudio_andShowsCelebrationDialog() = runTest {
        advanceUntilIdle()
        viewModel.onStartClicked()
        advanceUntilIdle()

        viewModel.onCompleteClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isActive)
        assertFalse(audioPlayer.playingState.value)
        assertTrue(state.showCelebrationDialog)
        assertEquals(25, state.completedXpEarned)
    }
}
