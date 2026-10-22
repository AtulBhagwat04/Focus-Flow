package com.focusflow.core.domain.model

/**
 * Domain model representing a planned focus task.
 *
 * Tracks the target focus duration, completed focus seconds,
 * and whether this task is currently active on the Focus screen.
 */
data class FocusTask(
    val id: String,
    val title: String,
    val durationMinutes: Int = 25,
    val completedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val isCurrentFocus: Boolean = false,
) {
    val completedMinutes: Int
        get() = completedSeconds / 60

    val progressFraction: Float
        get() {
            val targetSeconds = durationMinutes * 60
            return if (targetSeconds > 0) {
                (completedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }

    val remainingMinutes: Int
        get() = (durationMinutes - completedMinutes).coerceAtLeast(0)

    val formattedCompletedTime: String
        get() {
            val mins = completedSeconds / 60
            val secs = completedSeconds % 60
            return if (mins > 0) {
                "${mins}m ${secs}s"
            } else {
                "${secs}s"
            }
        }
}
