package com.focusflow.core.domain.audio

import com.focusflow.core.domain.model.AmbientSound
import kotlinx.coroutines.flow.StateFlow

/**
 * Audio player interface for focus session ambient soundscapes.
 *
 * Rules:
 * - Managed within the foreground service context to comply with Android 17 background audio restrictions.
 * - Pauses automatically when session is paused or audio focus is lost.
 */
interface AmbientAudioPlayer {

    val currentSound: StateFlow<AmbientSound>

    val isPlaying: StateFlow<Boolean>

    fun play(sound: AmbientSound)

    fun pause()

    fun resume()

    fun stop()

    fun release()
}
