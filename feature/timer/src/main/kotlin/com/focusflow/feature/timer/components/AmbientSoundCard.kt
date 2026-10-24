package com.focusflow.feature.timer.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Waves
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.feature.timer.R

/**
 * Editorial Ambient Sound Card displayed on the Focus screen.
 * Shows currently selected atmospheric sound, active playing indicator, and opens the selector.
 */
@Composable
fun AmbientSoundCard(
    selectedSound: AmbientSound,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlaying = selectedSound != AmbientSound.NONE

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_sound_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sound_pulse_alpha",
    )

    val soundIcon: ImageVector = when (selectedSound) {
        AmbientSound.RAIN -> Icons.Outlined.WaterDrop
        AmbientSound.FOREST -> Icons.Outlined.Spa
        AmbientSound.STREAM -> Icons.Outlined.Waves
        AmbientSound.COFFEE_SHOP -> Icons.Outlined.LocalCafe
        AmbientSound.WHITE_NOISE -> Icons.Outlined.GraphicEq
        AmbientSound.NONE -> Icons.Outlined.GraphicEq
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onCardClick,
            )
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, if (isPlaying) Color(0xFFCCE3D5) else Color(0xFFE2EBE5)),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isPlaying) Color(0xFFDCEFE3) else Color(0xFFEAF1EC)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = soundIcon,
                    contentDescription = null,
                    tint = if (isPlaying) FocusForestGreenDeep else Color(0xFF3B5D4B),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Status Subtitle
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.timer_ambient_sound),
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14291E),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPlaying) {
                        stringResource(R.string.timer_ambient_sound_playing, selectedSound.displayName)
                    } else {
                        stringResource(R.string.timer_ambient_sound_off)
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.5.sp,
                    fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isPlaying) FocusForestGreen else FocusMutedText,
                )
            }

            // Right side: Active pulse badge (if playing) + chevron
            if (isPlaying) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FocusForestMintBadge,
                    border = BorderStroke(1.dp, Color(0xFFBAE6CD)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A).copy(alpha = pulseAlpha)),
                        )
                        Text(
                            text = "Playing",
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusForestGreenDeep,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Navigation Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.timer_cd_ambient_sound),
                    tint = FocusForestGreenDeep,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
