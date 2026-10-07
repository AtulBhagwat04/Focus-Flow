package com.focusflow.feature.timer

import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState
import com.focusflow.core.domain.model.SubjectTag

data class TimerUiState(
    val session: FocusSession? = null,
    val selectedMode: SessionMode = SessionMode.Pomodoro(),
    val selectedTag: SubjectTag? = null,
    val availableTags: List<SubjectTag> = emptyList(),
    val selectedSound: AmbientSound = AmbientSound.NONE,
    val elapsedMs: Long = 0L,
    val remainingMs: Long = DEFAULT_POMODORO_DURATION_MS,
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val showCelebrationDialog: Boolean = false,
    val completedXpEarned: Int = 0,
) {
    val isActive: Boolean
        get() = session?.state?.isActive == true

    val formattedDisplayTime: String
        get() {
            val displayMs = if (selectedMode is SessionMode.Stopwatch) elapsedMs else remainingMs
            val totalSeconds = (displayMs / MILLIS_PER_SECOND).coerceAtLeast(0L)
            val minutes = totalSeconds / SECONDS_PER_MINUTE
            val seconds = totalSeconds % SECONDS_PER_MINUTE
            val hours = minutes / MINUTES_PER_HOUR

            return if (hours > 0) {
                String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", hours, minutes % MINUTES_PER_HOUR, seconds)
            } else {
                String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    companion object {
        const val MILLIS_PER_SECOND = 1000L
        const val SECONDS_PER_MINUTE = 60L
        const val MINUTES_PER_HOUR = 60L
        // 25 * 60 * 1000 = 1_500_000 ms
        const val DEFAULT_POMODORO_DURATION_MS = 1_500_000L
    }
}
