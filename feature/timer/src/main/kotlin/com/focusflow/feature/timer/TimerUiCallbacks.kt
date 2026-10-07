package com.focusflow.feature.timer

import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.core.domain.model.SubjectTag

/** Bundles all user-initiated event callbacks for [TimerScreen]. */
data class TimerUiCallbacks(
    val onStartClicked: () -> Unit,
    val onPauseClicked: () -> Unit,
    val onResumeClicked: () -> Unit,
    val onCompleteClicked: () -> Unit,
    val onSelectMode: (SessionMode) -> Unit,
    val onSelectTag: (SubjectTag) -> Unit,
    val onSelectSound: (AmbientSound) -> Unit,
    val onDismissCelebration: () -> Unit,
)
