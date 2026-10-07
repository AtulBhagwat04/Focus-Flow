package com.focusflow.feature.timer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusflow.core.domain.model.SessionMode
import com.focusflow.feature.timer.TimerUiState.Companion.MILLIS_PER_SECOND
import com.focusflow.feature.timer.TimerUiState.Companion.SECONDS_PER_MINUTE

private const val POMODORO_MINUTES = 25L
private const val SHORT_BREAK_MINUTES = 15L
private const val DEEP_WORK_MINUTES = 45L
private const val LONG_WORK_MINUTES = 60L

private val PRESET_MODES = listOf(
    "Pomodoro (25m)" to SessionMode.Pomodoro(POMODORO_MINUTES * SECONDS_PER_MINUTE * MILLIS_PER_SECOND),
    "Short (15m)" to SessionMode.Custom(SHORT_BREAK_MINUTES * SECONDS_PER_MINUTE * MILLIS_PER_SECOND),
    "Deep (45m)" to SessionMode.Custom(DEEP_WORK_MINUTES * SECONDS_PER_MINUTE * MILLIS_PER_SECOND),
    "Long (60m)" to SessionMode.Custom(LONG_WORK_MINUTES * SECONDS_PER_MINUTE * MILLIS_PER_SECOND),
    "Stopwatch" to SessionMode.Stopwatch,
)

@Composable
fun ModeSelectorRow(
    selectedMode: SessionMode,
    enabled: Boolean,
    onSelectMode: (SessionMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PRESET_MODES.forEach { (label, mode) ->
            val isSelected = when {
                mode is SessionMode.Pomodoro && selectedMode is SessionMode.Pomodoro -> true
                mode is SessionMode.Stopwatch && selectedMode is SessionMode.Stopwatch -> true
                mode is SessionMode.Custom && selectedMode is SessionMode.Custom ->
                    mode.durationMs == selectedMode.durationMs
                else -> false
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectMode(mode) },
                label = { Text(label) },
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
