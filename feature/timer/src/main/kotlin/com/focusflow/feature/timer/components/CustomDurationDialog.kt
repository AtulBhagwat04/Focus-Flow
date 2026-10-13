package com.focusflow.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import kotlin.math.roundToInt

private val QUICK_OPTIONS = listOf(15, 30, 45, 60, 75, 120)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomDurationDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(currentMinutes.coerceIn(5, 180).toFloat()) }
    val selectedMinutes = (sliderValue / 5f).roundToInt() * 5

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Set Custom Duration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose how long you want to focus:",
                    fontSize = 13.5.sp,
                    color = FocusMutedText,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big duration display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$selectedMinutes",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FocusForestGreenDeep,
                    )
                    Text(
                        text = " min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusForestGreen,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slider
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 5f..180f,
                    steps = 34, // 5 to 180 by 5
                    colors = SliderDefaults.colors(
                        thumbColor = FocusForestGreenDeep,
                        activeTrackColor = FocusForestGreen,
                        inactiveTrackColor = Color(0xFFE2E8F0),
                    ),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QUICK_OPTIONS.forEach { option ->
                        val isChipSelected = selectedMinutes == option
                        FilterChip(
                            selected = isChipSelected,
                            onClick = { sliderValue = option.toFloat() },
                            label = { Text("${option}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FocusForestMintBg,
                                selectedLabelColor = FocusForestGreenDeep,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMinutes) },
                colors = ButtonDefaults.buttonColors(containerColor = FocusForestGreenDeep),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Set Duration", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FocusMutedText)
            }
        },
    )
}
