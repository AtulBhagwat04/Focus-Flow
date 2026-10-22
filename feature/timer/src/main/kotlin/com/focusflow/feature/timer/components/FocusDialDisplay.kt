package com.focusflow.feature.timer.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.component.FocusSproutIcon
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.TimerFontFamily
import java.util.Locale
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private val DIAL_SIZE = 270.dp
private val TRACK_STROKE_WIDTH = 14.dp
private val IDLE_KNOB_RADIUS = 7.5.dp
private val DRAGGING_KNOB_RADIUS = 12.dp
private val MAX_KNOB_RADIUS = 12.dp
private val MILESTONE_MINUTES = listOf(15, 30, 45, 60)

/**
 * Clean, minimalist circular focus dial where a complete circle = 1 hr (60 minutes).
 *
 * Key behaviors:
 * 1. Touch swiping is strictly constrained to the circular dial ring (± 24dp from track radius).
 *    Touches outside the ring are never consumed, avoiding accidental adjustments while scrolling.
 * 2. Swiping clockwise to a full circle locks cleanly at 60 minutes (1 hr) without ever rolling
 *    over or restarting from 0.
 * 3. Haptic vibration triggers whenever the user swipes to or crosses 15, 30, 45, or 60 minutes.
 * 4. The sliding knob thumb starts at a sleek, compact size (7.5dp) at idle and dynamically scales up
 *    to 12dp while dragging, smoothly returning to its original size upon release.
 * 5. Perfectly centered alignment: Sprout icon, timer digits, and normal text "Tap To Customize" hint.
 */
@Composable
fun FocusDialDisplay(
    formattedTime: String,
    statusLabel: String,
    isActive: Boolean,
    progressFraction: Float,
    selectedDurationMinutes: Int,
    modifier: Modifier = Modifier,
    isCustomTimeEnabled: Boolean = true,
    onDurationChange: (Int) -> Unit = {},
    onCustomTimeClick: () -> Unit = {},
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragMinutes by remember { mutableIntStateOf(selectedDurationMinutes.coerceIn(1, 60)) }

    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Keep dragMinutes synchronized with external updates when not actively dragging
    LaunchedEffect(selectedDurationMinutes, isDragging) {
        if (!isDragging) {
            dragMinutes = selectedDurationMinutes.coerceIn(1, 60)
        }
    }

    // Dynamic knob radius: small at idle (7.5dp), expands while sliding (12dp), shrinks on release
    val animatedKnobRadiusDp by animateDpAsState(
        targetValue = if (isDragging) DRAGGING_KNOB_RADIUS else IDLE_KNOB_RADIUS,
        animationSpec = tween(durationMillis = 180),
        label = "knob_radius",
    )

    // Complete circle = 60 minutes = 360 degrees.
    // If selectedDurationMinutes > 60 (custom session > 1h), arc is complete 360°.
    val targetSweepAngle = if (isActive) {
        (progressFraction.coerceIn(0f, 1f) * 360f).coerceAtLeast(6f)
    } else {
        val minutesToDisplay = if (isDragging) dragMinutes else selectedDurationMinutes
        if (minutesToDisplay <= 60) {
            (minutesToDisplay / 60f * 360f).coerceIn(6f, 360f)
        } else {
            360f
        }
    }

    val animatedSweepAngle by animateFloatAsState(
        targetValue = targetSweepAngle,
        animationSpec = tween(durationMillis = 350),
        label = "dial_sweep_angle",
    )

    // Direct tracking during drag for instant responsiveness, smooth animation otherwise
    val currentSweepAngle = if (isDragging) targetSweepAngle else animatedSweepAngle

    val displayDigits = if (isDragging) {
        String.format(Locale.getDefault(), "%02d:00", dragMinutes)
    } else {
        formattedTime
    }

    Box(
        modifier = modifier
            .size(DIAL_SIZE)
            .semantics {
                contentDescription = if (!isActive) {
                    if (isCustomTimeEnabled) {
                        "$selectedDurationMinutes minutes focus session. Drag dial ring to adjust duration, or tap timer to customize."
                    } else {
                        "$selectedDurationMinutes minutes task focus session."
                    }
                } else {
                    "$statusLabel: $formattedTime"
                }
            }
            .pointerInput(isActive, isCustomTimeEnabled) {
                if (isActive || !isCustomTimeEnabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val center = Offset(size.width / 2f, size.height / 2f)

                    val strokeWidthPx = TRACK_STROKE_WIDTH.toPx()
                    val maxKnobRadiusPx = MAX_KNOB_RADIUS.toPx()
                    val arcPadding = strokeWidthPx / 2f + maxKnobRadiusPx + 4.dp.toPx()
                    val radius = (size.width - 2 * arcPadding) / 2f

                    // Strictly constrain touch down to the circular dial ring (± 24dp from track radius)
                    val touchRingInner = radius - 24.dp.toPx()
                    val touchRingOuter = radius + 24.dp.toPx()

                    val dx = down.position.x - center.x
                    val dy = down.position.y - center.y
                    val initialDist = hypot(dx, dy)

                    if (initialDist in touchRingInner..touchRingOuter) {
                        down.consume()
                        isDragging = true

                        var lastClockAngle = calculateClockAngle(down.position, center)
                        var accumulatedMinutes = dragMinutes.toFloat()

                        // Check initial touch on ring
                        val initialTouchedMinute = (lastClockAngle / 360f * 60f).roundToInt().coerceIn(1, 60)
                        if (initialTouchedMinute != dragMinutes) {
                            checkAndTriggerMilestone(context, hapticFeedback, dragMinutes, initialTouchedMinute)
                            dragMinutes = initialTouchedMinute
                            accumulatedMinutes = initialTouchedMinute.toFloat()
                            onDurationChange(initialTouchedMinute)
                        }

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (change.pressed) {
                                val currentClockAngle = calculateClockAngle(change.position, center)
                                val rawDelta = currentClockAngle - lastClockAngle
                                val deltaAngle = when {
                                    rawDelta > 180f -> rawDelta - 360f
                                    rawDelta < -180f -> rawDelta + 360f
                                    else -> rawDelta
                                }
                                lastClockAngle = currentClockAngle

                                val minutesDelta = (deltaAngle / 360f) * 60f
                                val newAccumulated = (accumulatedMinutes + minutesDelta).coerceIn(1f, 60f)
                                accumulatedMinutes = newAccumulated

                                val newMinutes = newAccumulated.roundToInt().coerceIn(1, 60)
                                if (newMinutes != dragMinutes) {
                                    checkAndTriggerMilestone(context, hapticFeedback, dragMinutes, newMinutes)
                                    dragMinutes = newMinutes
                                    onDurationChange(newMinutes)
                                }
                                change.consume()
                            } else {
                                break
                            }
                        }
                        isDragging = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(DIAL_SIZE)) {
            val strokeWidthPx = TRACK_STROKE_WIDTH.toPx()
            val maxKnobRadiusPx = MAX_KNOB_RADIUS.toPx()
            val knobRadiusPx = animatedKnobRadiusDp.toPx()
            val knobBorderPx = (knobRadiusPx * 0.28f).coerceIn(2.dp.toPx(), 3.5.dp.toPx())
            val innerDotRadiusPx = (knobRadiusPx * 0.24f).coerceIn(1.8.dp.toPx(), 3.dp.toPx())

            val arcPadding = strokeWidthPx / 2f + maxKnobRadiusPx + 4.dp.toPx()
            val arcSize = Size(
                width = size.width - 2 * arcPadding,
                height = size.height - 2 * arcPadding,
            )
            val arcTopLeft = Offset(arcPadding, arcPadding)
            val radius = arcSize.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 0. Inner white circular disc background for crisp high contrast
            drawCircle(
                color = Color.White.copy(alpha = 0.92f),
                radius = radius - strokeWidthPx / 2f + 1.dp.toPx(),
                center = center,
            )

            // 1. Background circular track (clean soft muted green/gray)
            drawArc(
                color = Color(0xFFE2EBE5),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // 2. Active green progress arc (starts at 12 o'clock = -90 degrees)
            val startAngle = -90f
            drawArc(
                color = FocusForestGreenDeep,
                startAngle = startAngle,
                sweepAngle = currentSweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // 3. Circular knob thumb indicator at the tip of the active arc
            val knobAngleDeg = startAngle + currentSweepAngle
            val knobAngleRad = Math.toRadians(knobAngleDeg.toDouble())
            val knobX = (center.x + radius * cos(knobAngleRad)).toFloat()
            val knobY = (center.y + radius * sin(knobAngleRad)).toFloat()

            // Subtle glow ring when idle to indicate draggable control on the ring, expanding when dragged
            if (!isActive && isCustomTimeEnabled) {
                drawCircle(
                    color = FocusForestGreen.copy(alpha = if (isDragging) 0.28f else 0.16f),
                    radius = knobRadiusPx + (if (isDragging) 6.dp.toPx() else 4.dp.toPx()),
                    center = Offset(knobX, knobY),
                )
            }

            // Knob drop shadow
            drawCircle(
                color = Color(0x22000000),
                radius = knobRadiusPx + 1.5.dp.toPx(),
                center = Offset(knobX, knobY + 1.2.dp.toPx()),
            )

            // Knob white center fill
            drawCircle(
                color = Color.White,
                radius = knobRadiusPx,
                center = Offset(knobX, knobY),
            )

            // Knob deep forest green border
            drawCircle(
                color = FocusForestGreenDeep,
                radius = knobRadiusPx,
                center = Offset(knobX, knobY),
                style = Stroke(width = knobBorderPx),
            )
        }

        // Inner dial content: Sprout Icon, Big Digits, and "Tap to customize" normal text hint
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    enabled = !isActive && isCustomTimeEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onCustomTimeClick,
                )
                .semantics {
                    if (!isActive && isCustomTimeEnabled) {
                        role = Role.Button
                        contentDescription = "Timer: $displayDigits. Tap to customize time."
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            FocusSproutIcon(
                modifier = Modifier.size(28.dp),
                tint = FocusForestGreenDeep,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = displayDigits,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF14291E),
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
            )

            if (!isActive && isCustomTimeEnabled) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to customize",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FocusMutedText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Calculates clock angle in [0f, 360f) with 12 o'clock = 0°, rotating clockwise.
 */
private fun calculateClockAngle(touch: Offset, center: Offset): Float {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val angleRad = kotlin.math.atan2(dy, dx)
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

    var clockAngle = (angleDeg + 90f) % 360f
    if (clockAngle < 0f) clockAngle += 360f
    return clockAngle
}

/**
 * Triggers haptic vibration whenever swiping hits or crosses 15, 30, 45, or 60 minutes.
 */
private fun checkAndTriggerMilestone(
    context: Context,
    hapticFeedback: HapticFeedback,
    oldMinutes: Int,
    newMinutes: Int,
) {
    if (oldMinutes == newMinutes) return
    val hitMilestone = MILESTONE_MINUTES.any { milestone ->
        (oldMinutes < milestone && newMinutes >= milestone) ||
        (oldMinutes > milestone && newMinutes <= milestone) ||
        newMinutes == milestone
    }

    if (hitMilestone) {
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(
                        VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(30L)
                }
            }
        } catch (_: Exception) {
            // Degrades safely
        }
    }
}
