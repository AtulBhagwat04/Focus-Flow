package com.focusflow.feature.timer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusflow.core.domain.model.AmbientSound

@Composable
fun AmbientSoundSelector(
    selectedSound: AmbientSound,
    onSelectSound: (AmbientSound) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AmbientSound.entries.forEach { sound ->
            val isSelected = sound == selectedSound

            FilterChip(
                selected = isSelected,
                onClick = { onSelectSound(sound) },
                leadingIcon = {
                    if (sound != AmbientSound.NONE) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = null,
                        )
                    }
                },
                label = { Text(sound.displayName) },
            )
        }
    }
}
