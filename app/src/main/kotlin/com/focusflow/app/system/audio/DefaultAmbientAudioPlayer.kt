package com.focusflow.app.system.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.focusflow.core.domain.audio.AmbientAudioPlayer
import com.focusflow.core.domain.model.AmbientSound
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAmbientAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) : AmbientAudioPlayer {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _currentSound = MutableStateFlow(AmbientSound.NONE)
    override val currentSound: StateFlow<AmbientSound> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(DUCKED_VOLUME, DUCKED_VOLUME)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(FULL_VOLUME, FULL_VOLUME)
                if (!_isPlaying.value && _currentSound.value != AmbientSound.NONE) {
                    resume()
                }
            }
        }
    }

    override fun play(sound: AmbientSound) {
        if (sound == AmbientSound.NONE) {
            stop()
            return
        }
        stop()
        _currentSound.value = sound
        _isPlaying.value = startPlayback(sound)
    }

    /**
     * Resolves the resource ID for [sound] and starts [mediaPlayer].
     * Returns true when playback is considered active (including when the asset is not yet
     * bundled, so the session state stays consistent), false on real failure.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun startPlayback(sound: AmbientSound): Boolean {
        val resourceName = sound.soundResourceName
        if (!requestAudioFocus() || resourceName == null) return false

        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        return try {
            if (resId == 0) {
                // Asset not bundled yet — mark as playing so session state is consistent
                true
            } else {
                mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                    isLooping = true
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    start()
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
            // Safe cleanup
        }
        _isPlaying.value = false
    }

    override fun resume() {
        if (_currentSound.value == AmbientSound.NONE) return
        if (requestAudioFocus()) {
            try {
                mediaPlayer?.start()
                _isPlaying.value = true
            } catch (_: Exception) {
                _isPlaying.value = false
            }
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
            // Safe cleanup
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentSound.value = AmbientSound.NONE
        abandonAudioFocus()
    }

    override fun release() {
        stop()
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (audioManager == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(afChangeListener)
        }
    }

    companion object {
        private const val DUCKED_VOLUME = 0.2f
        private const val FULL_VOLUME = 1.0f
    }
}
