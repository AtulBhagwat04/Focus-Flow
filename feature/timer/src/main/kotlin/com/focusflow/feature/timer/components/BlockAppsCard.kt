package com.focusflow.feature.timer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.timer.R

/**
 * Compact, user-friendly Strict Focus Mode toggle card.
 *
 * Aligned directly below the primary timer action button.
 * Unwanted navigation elements removed for an uncluttered, focused experience.
 */
@Composable
fun BlockAppsCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isSessionActive: Boolean = false,
    blockedAppsCount: Int = 0,
    onNavigateToLimits: (() -> Unit)? = null,
) {
    val title = if (isEnabled) {
        stringResource(R.string.timer_strict_mode_title)
    } else {
        stringResource(R.string.timer_selective_mode_title)
    }

    val subtitle = when {
        isEnabled && isSessionActive -> stringResource(R.string.timer_strict_mode_subtitle_active)
        isEnabled -> stringResource(R.string.timer_strict_mode_subtitle_enabled)
        blockedAppsCount > 0 -> stringResource(R.string.timer_strict_mode_subtitle_disabled_count, blockedAppsCount)
        else -> stringResource(R.string.timer_strict_mode_subtitle_disabled)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = !isSessionActive,
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = { onToggle(!isEnabled) },
            )
            .semantics { role = Role.Switch },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFFCCE3D5) else Color(0xFFE2EBE5),
        ),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Compact Icon Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isEnabled) Color(0xFFDCEFE3) else Color(0xFFF0F4F1)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = if (isEnabled) FocusForestGreenDeep else Color(0xFF5A7263),
                    modifier = Modifier.size(17.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF14291E),
                )
                Text(
                    text = subtitle,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isEnabled) FocusForestGreen else FocusMutedText,
                )
            }

            if (!isEnabled && onNavigateToLimits != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEAF4EE),
                    border = BorderStroke(0.5.dp, Color(0xFFC7DEC2)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            enabled = !isSessionActive,
                            onClick = onNavigateToLimits,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.timer_strict_mode_choose_apps),
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusForestGreenDeep,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Configure blocked apps",
                            tint = FocusForestGreenDeep,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Compact Switch Toggle
            Switch(
                checked = isEnabled,
                onCheckedChange = { if (!isSessionActive) onToggle(it) },
                enabled = !isSessionActive,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FocusForestGreenDeep,
                    checkedBorderColor = Color.Transparent,
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.9f),
                    disabledCheckedTrackColor = FocusForestGreenDeep.copy(alpha = 0.8f),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD4E0D9),
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}
