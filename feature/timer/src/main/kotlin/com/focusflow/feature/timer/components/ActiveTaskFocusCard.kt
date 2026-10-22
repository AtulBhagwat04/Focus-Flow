package com.focusflow.feature.timer.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.feature.timer.R

/**
 * Dedicated task card displayed on the Focus screen when a user starts focus for a specific task.
 * Shows the task title, active focus status, elapsed progress, and allows clearing when idle.
 */
@Composable
fun ActiveTaskFocusCard(
    task: FocusTask,
    isRunning: Boolean,
    isPaused: Boolean,
    onClearTask: () -> Unit,
    canClear: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_task_indicator")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x101B4E36),
                spotColor = Color(0x1A1B4E36),
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF3F8F5),
        border = BorderStroke(1.2.dp, Color(0xFFCEE2D5)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Header Row: Label + Status Pill + Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = FocusForestGreenDeep,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.timer_active_task_label),
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusForestGreenDeep,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isRunning -> FocusForestGreenDeep
                            isPaused -> Color(0xFFE2A03F)
                            else -> FocusForestMintBadge
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isRunning) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF67D5A4).copy(alpha = pulseAlpha)),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = when {
                                    isRunning -> stringResource(R.string.timer_task_status_focusing)
                                    isPaused -> stringResource(R.string.timer_task_status_paused)
                                    else -> stringResource(R.string.timer_task_status_ready)
                                },
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning || isPaused) Color.White else FocusForestGreenDeep,
                            )
                        }
                    }

                    if (canClear) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = onClearTask,
                                )
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Clear active task"
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = FocusMutedText,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Task Title
            Text(
                text = task.title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FocusDarkText,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { task.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = FocusForestGreenDeep,
                trackColor = Color(0xFFDCE9E0),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${task.formattedCompletedTime} completed",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusForestGreen,
                )
                Text(
                    text = "${task.durationMinutes}m target (${(task.progressFraction * 100).toInt()}%)",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.5.sp,
                    color = FocusMutedText,
                )
            }
        }
    }
}
