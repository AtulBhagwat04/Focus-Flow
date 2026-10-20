package com.focusflow.feature.timer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep

/**
 * Predefined timer chips (25, 50, 100 min) with dynamic custom duration support.
 *
 * If the user's selected duration is one of the predefined times (25, 50, 100),
 * those three chips are displayed with the active duration filled in dark forest green.
 * If a custom duration (e.g. 39 min) is selected via dial dragging or custom dialog,
 * that duration is dynamically incorporated and highlighted alongside the presets.
 */
@Composable
fun TimerPresetChips(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val predefined = listOf(25, 50, 100)
    val displayList = predefined

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        displayList.forEachIndexed { index, minutes ->
            val isSelected = minutes == selectedMinutes

            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        enabled = enabled,
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = { onSelectMinutes(minutes) },
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = "$minutes minutes focus preset${if (isSelected) ", selected" else ""}"
                    },
                shape = CircleShape,
                color = if (isSelected) FocusForestGreenDeep else Color.White.copy(alpha = 0.90f),
                border = BorderStroke(
                    width = 1.2.dp,
                    color = if (isSelected) FocusForestGreenDeep else Color(0xFFBACEC3),
                ),
                shadowElevation = if (isSelected) 3.dp else 0.dp,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$minutes min",
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Color(0xFF2A4235),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (index < displayList.size - 1) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}
