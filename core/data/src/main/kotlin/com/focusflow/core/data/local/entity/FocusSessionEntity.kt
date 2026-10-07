package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusflow.core.domain.model.FocusSession
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SessionState

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val modeType: String,
    val modeWorkDurationMs: Long = 0L,
    val modeBreakDurationMs: Long = 0L,
    val modeCustomDurationMs: Long = 0L,
    val modeTargetEpochMs: Long = 0L,
    val state: String,
    val tagId: String? = null,
    val targetDurationMs: Long,
    val startedAtEpochMs: Long,
    val pausedAtEpochMs: Long? = null,
    val totalPausedDurationMs: Long = 0L,
    val completedAtEpochMs: Long? = null,
    val xpEarned: Int = 0,
)

fun FocusSessionEntity.asDomainModel(): FocusSession {
    val domainMode: SessionMode = when (modeType) {
        "POMODORO" -> SessionMode.Pomodoro(
            workDurationMs = modeWorkDurationMs,
            breakDurationMs = modeBreakDurationMs,
        )
        "STOPWATCH" -> SessionMode.Stopwatch
        "CUSTOM" -> SessionMode.Custom(durationMs = modeCustomDurationMs)
        "UNTIL_TIME" -> SessionMode.UntilTime(targetEpochMs = modeTargetEpochMs)
        else -> SessionMode.Pomodoro()
    }

    val domainState: SessionState = try {
        SessionState.valueOf(state)
    } catch (_: IllegalArgumentException) {
        SessionState.IDLE
    }

    return FocusSession(
        id = id,
        mode = domainMode,
        state = domainState,
        tagId = tagId,
        targetDurationMs = targetDurationMs,
        startedAtEpochMs = startedAtEpochMs,
        pausedAtEpochMs = pausedAtEpochMs,
        totalPausedDurationMs = totalPausedDurationMs,
        completedAtEpochMs = completedAtEpochMs,
        xpEarned = xpEarned,
    )
}

fun FocusSession.asEntity(): FocusSessionEntity {
    val modeType = when (mode) {
        is SessionMode.Pomodoro -> "POMODORO"
        is SessionMode.Stopwatch -> "STOPWATCH"
        is SessionMode.Custom -> "CUSTOM"
        is SessionMode.UntilTime -> "UNTIL_TIME"
    }

    val workMs = (mode as? SessionMode.Pomodoro)?.workDurationMs ?: 0L
    val breakMs = (mode as? SessionMode.Pomodoro)?.breakDurationMs ?: 0L
    val customMs = (mode as? SessionMode.Custom)?.durationMs ?: 0L
    val targetEpochMs = (mode as? SessionMode.UntilTime)?.targetEpochMs ?: 0L

    return FocusSessionEntity(
        id = id,
        modeType = modeType,
        modeWorkDurationMs = workMs,
        modeBreakDurationMs = breakMs,
        modeCustomDurationMs = customMs,
        modeTargetEpochMs = targetEpochMs,
        state = state.name,
        tagId = tagId,
        targetDurationMs = targetDurationMs,
        startedAtEpochMs = startedAtEpochMs,
        pausedAtEpochMs = pausedAtEpochMs,
        totalPausedDurationMs = totalPausedDurationMs,
        completedAtEpochMs = completedAtEpochMs,
        xpEarned = xpEarned,
    )
}
