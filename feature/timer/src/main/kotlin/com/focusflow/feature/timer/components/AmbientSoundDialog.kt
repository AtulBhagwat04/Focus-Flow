package com.focusflow.feature.timer.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.feature.timer.R

/**
 * Editorial Ambient Sound Selection Dialog with refined sound cards,
 * atmospheric descriptions, dedicated icons, and clear active selection states.
 */
@Composable
fun AmbientSoundDialog(
    selectedSound: AmbientSound,
    onSelectSound: (AmbientSound) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, Color(0xFFCCE3D5)),
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                // ── Header Row: Title + Close Button ─────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FocusForestMintBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = null,
                                tint = FocusForestGreenDeep,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Text(
                            text = stringResource(R.string.timer_ambient_sound),
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14291E),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F6F3))
                            .clickable(
                                indication = ripple(bounded = true, radius = 16.dp),
                                interactionSource = null,
                                onClick = onDismiss,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF2E483A),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.timer_ambient_dialog_subtitle),
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = FocusMutedText,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Sound Selection Cards List ───────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AmbientSound.entries.forEach { sound ->
                        val isSelected = sound == selectedSound
                        val (icon, descriptionRes) = getSoundMeta(sound)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = { onSelectSound(sound) },
                                )
                                .semantics {
                                    role = Role.RadioButton
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFF0F7F3) else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) FocusForestGreenDeep else Color(0xFFE2EBE5),
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Sound Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) FocusForestMintBg else Color(0xFFF4F7F5)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) FocusForestGreenDeep else Color(0xFF5A6E63),
                                        modifier = Modifier.size(19.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Sound Display Name & Atmospheric Description
                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = sound.displayName,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) FocusForestGreenDeep else Color(0xFF14291E),
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = stringResource(descriptionRes),
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        color = if (isSelected) FocusForestGreen else FocusMutedText,
                                        lineHeight = 15.sp,
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Radio Selection Pill / Circle
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(FocusForestGreenDeep),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .padding(1.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(20.dp),
                                            shape = CircleShape,
                                            color = Color.Transparent,
                                            border = BorderStroke(1.5.dp, Color(0xFFCCE3D5)),
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Footer CTA Button: Done ─────────────────────────────────
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusForestGreenDeep,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.timer_done),
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private fun getSoundMeta(sound: AmbientSound): Pair<ImageVector, Int> {
    return when (sound) {
        AmbientSound.NONE -> Icons.AutoMirrored.Outlined.VolumeOff to R.string.timer_sound_desc_none
        AmbientSound.WHITE_NOISE -> Icons.Outlined.GraphicEq to R.string.timer_sound_desc_white_noise
        AmbientSound.RAIN -> Icons.Outlined.WaterDrop to R.string.timer_sound_desc_rain
        AmbientSound.FOREST -> Icons.Outlined.Spa to R.string.timer_sound_desc_forest
        AmbientSound.STREAM -> Icons.Outlined.Waves to R.string.timer_sound_desc_stream
        AmbientSound.COFFEE_SHOP -> Icons.Outlined.LocalCafe to R.string.timer_sound_desc_coffee_shop
    }
}
