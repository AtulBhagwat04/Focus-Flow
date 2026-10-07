package com.focusflow.feature.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.feature.timer.components.AmbientSoundSelector
import com.focusflow.feature.timer.components.CompletionDialog
import com.focusflow.feature.timer.components.ModeSelectorRow
import com.focusflow.feature.timer.components.TabularTimerDisplay
import com.focusflow.feature.timer.components.TagSelectorRow

private const val START_BUTTON_WIDTH_FRACTION = 0.7f
private val BUTTON_HEIGHT_PRIMARY = 56.dp
private val BUTTON_HEIGHT_SECONDARY = 52.dp
private val BUTTON_SPACING = 16.dp
private val ACTION_ROW_PADDING = 24.dp

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimerScreenContent(
        uiState = uiState,
        callbacks = TimerUiCallbacks(
            onStartClicked = viewModel::onStartClicked,
            onPauseClicked = viewModel::onPauseClicked,
            onResumeClicked = viewModel::onResumeClicked,
            onCompleteClicked = viewModel::onCompleteClicked,
            onSelectMode = viewModel::onSelectMode,
            onSelectTag = viewModel::onSelectTag,
            onSelectSound = viewModel::onSelectSound,
            onDismissCelebration = viewModel::onDismissCelebration,
        ),
        modifier = modifier,
    )
}

@Composable
fun TimerScreenContent(
    uiState: TimerUiState,
    callbacks: TimerUiCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TagSelectorRow(
                tags = uiState.availableTags,
                selectedTag = uiState.selectedTag,
                enabled = !uiState.isActive,
                onSelectTag = callbacks.onSelectTag,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ModeSelectorRow(
                selectedMode = uiState.selectedMode,
                enabled = !uiState.isActive,
                onSelectMode = callbacks.onSelectMode,
            )
        }

        TabularTimerDisplay(
            formattedTime = uiState.formattedDisplayTime,
            progress = uiState.progress,
            statusLabel = when {
                uiState.isPaused -> "Paused"
                uiState.isRunning -> "Focusing"
                else -> "Ready to Focus"
            },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AmbientSoundSelector(
                selectedSound = uiState.selectedSound,
                onSelectSound = callbacks.onSelectSound,
            )
            Spacer(modifier = Modifier.height(24.dp))
            TimerActionButtons(uiState = uiState, callbacks = callbacks)
        }
    }

    if (uiState.showCelebrationDialog) {
        CompletionDialog(
            xpEarned = uiState.completedXpEarned,
            onDismiss = callbacks.onDismissCelebration,
        )
    }
}

@Composable
private fun TimerActionButtons(uiState: TimerUiState, callbacks: TimerUiCallbacks) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ACTION_ROW_PADDING),
        horizontalArrangement = Arrangement.Center,
    ) {
        when {
            !uiState.isActive -> StartButton(callbacks.onStartClicked)
            uiState.isRunning -> RunningButtons(callbacks.onPauseClicked, callbacks.onCompleteClicked)
            uiState.isPaused -> PausedButtons(callbacks.onResumeClicked, callbacks.onCompleteClicked)
        }
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(START_BUTTON_WIDTH_FRACTION)
            .height(BUTTON_HEIGHT_PRIMARY),
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Start Focus", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RowScope.RunningButtons(onPause: () -> Unit, onComplete: () -> Unit) {
    OutlinedButton(
        onClick = onPause,
        modifier = Modifier.weight(1f).height(BUTTON_HEIGHT_SECONDARY),
    ) {
        Icon(Icons.Default.Pause, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pause")
    }
    Spacer(modifier = Modifier.width(BUTTON_SPACING))
    Button(
        onClick = onComplete,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.weight(1f).height(BUTTON_HEIGHT_SECONDARY),
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Finish")
    }
}

@Composable
private fun RowScope.PausedButtons(onResume: () -> Unit, onComplete: () -> Unit) {
    Button(
        onClick = onResume,
        modifier = Modifier.weight(1f).height(BUTTON_HEIGHT_SECONDARY),
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Resume")
    }
    Spacer(modifier = Modifier.width(BUTTON_SPACING))
    OutlinedButton(
        onClick = onComplete,
        modifier = Modifier.weight(1f).height(BUTTON_HEIGHT_SECONDARY),
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Finish")
    }
}

@Preview(showBackground = true, name = "Timer — Idle")
@Composable
private fun TimerScreenIdlePreview() {
    FocusFlowTheme {
        TimerScreenContent(
            uiState = TimerUiState(),
            callbacks = TimerUiCallbacks(
                onStartClicked = {},
                onPauseClicked = {},
                onResumeClicked = {},
                onCompleteClicked = {},
                onSelectMode = {},
                onSelectTag = {},
                onSelectSound = {},
                onDismissCelebration = {},
            ),
        )
    }
}
