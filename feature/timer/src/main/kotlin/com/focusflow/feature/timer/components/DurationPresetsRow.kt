package com.focusflow.feature.timer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText

private val PRESET_MINUTES = listOf(25, 50, 90)

/**
 * 4-card duration preset row matching the reference design:
 * [25 min] [50 min] [90 min] [Custom]
 */
@Composable
fun DurationPresetsRow(
    selectedMinutes: Int,
    enabled: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onCustomClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCustomSelected = selectedMinutes !in PRESET_MINUTES

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PRESET_MINUTES.forEach { minutes ->
            val isSelected = selectedMinutes == minutes
            PresetCard(
                minutes = minutes,
                isSelected = isSelected,
                enabled = enabled,
                onClick = { onSelectMinutes(minutes) },
                modifier = Modifier.weight(1f),
            )
        }

        CustomPresetCard(
            isSelected = isCustomSelected,
            selectedMinutes = if (isCustomSelected) selectedMinutes else null,
            enabled = enabled,
            onClick = onCustomClicked,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PresetCard(
    minutes: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBg else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "preset_bg_$minutes",
    )
    val borderColor = if (isSelected) FocusForestMintBorder else Color(0xFFE5EDE8)
    val numberColor = if (isSelected) FocusForestGreenDeep else FocusDarkText
    val unitColor = if (isSelected) FocusForestGreen else FocusMutedText

    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                enabled = enabled,
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                contentDescription = "$minutes minutes"
            },
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 1.dp else 0.5.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = minutes.toString(),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor,
                lineHeight = 20.sp,
            )
            Text(
                text = "min",
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = unitColor,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
private fun CustomPresetCard(
    isSelected: Boolean,
    selectedMinutes: Int?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBg else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "preset_custom_bg",
    )
    val borderColor = if (isSelected) FocusForestMintBorder else Color(0xFFE5EDE8)
    val contentColor = if (isSelected) FocusForestGreenDeep else FocusDarkText

    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                enabled = enabled,
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                contentDescription = "Custom duration"
            },
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 1.dp else 0.5.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (selectedMinutes != null) "${selectedMinutes}m" else "Custom",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
            )
        }
    }
}
