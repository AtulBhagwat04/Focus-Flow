package com.focusflow.feature.timer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.component.FocusSproutIcon
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.TimerFontFamily
import kotlin.math.cos
import kotlin.math.sin

private val DIAL_SIZE = 268.dp
private val TRACK_STROKE_WIDTH = 13.dp
private val KNOB_RADIUS = 10.dp
private val KNOB_BORDER_WIDTH = 3.dp

/**
 * Circular focus dial display with a leaf sprout, tabular time digits,
 * active green arc, and a circular thumb knob matching the reference design.
 */
@Composable
fun FocusDialDisplay(
    formattedTime: String,
    statusLabel: String,
    isActive: Boolean,
    progressFraction: Float,
    selectedDurationMinutes: Int,
    modifier: Modifier = Modifier,
) {
    // When idle, dial arc visually reflects the selected duration (25m = ~105° in mockup)
    val targetSweepAngle = if (isActive) {
        (progressFraction.coerceIn(0f, 1f) * 360f).coerceAtLeast(6f)
    } else {
        when (selectedDurationMinutes) {
            25 -> 105f
            50 -> 195f
            90 -> 285f
            else -> ((selectedDurationMinutes / 90f).coerceIn(0.12f, 1f) * 300f)
        }
    }

    val animatedSweepAngle by animateFloatAsState(
        targetValue = targetSweepAngle,
        animationSpec = tween(durationMillis = 400),
        label = "dial_sweep_angle",
    )

    Box(
        modifier = modifier.size(DIAL_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(DIAL_SIZE)) {
            val strokeWidthPx = TRACK_STROKE_WIDTH.toPx()
            val knobRadiusPx = KNOB_RADIUS.toPx()
            val knobBorderPx = KNOB_BORDER_WIDTH.toPx()

            val arcPadding = strokeWidthPx / 2f + knobRadiusPx / 2f
            val arcSize = Size(
                width = size.width - 2 * arcPadding,
                height = size.height - 2 * arcPadding,
            )
            val arcTopLeft = Offset(arcPadding, arcPadding)
            val radius = arcSize.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Background circular track (soft muted light green/gray)
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
                color = FocusForestGreen,
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // 3. Circular knob thumb indicator at the tip of the active arc
            val knobAngleDeg = startAngle + animatedSweepAngle
            val knobAngleRad = Math.toRadians(knobAngleDeg.toDouble())
            val knobX = (center.x + radius * cos(knobAngleRad)).toFloat()
            val knobY = (center.y + radius * sin(knobAngleRad)).toFloat()

            // Subtle drop shadow around knob
            drawCircle(
                color = Color(0x20000000),
                radius = knobRadiusPx + 1.5.dp.toPx(),
                center = Offset(knobX, knobY + 1.dp.toPx()),
            )

            // Knob white fill
            drawCircle(
                color = Color.White,
                radius = knobRadiusPx,
                center = Offset(knobX, knobY),
            )

            // Knob green border
            drawCircle(
                color = FocusForestGreen,
                radius = knobRadiusPx,
                center = Offset(knobX, knobY),
                style = Stroke(width = knobBorderPx),
            )
        }

        // Inner dial content: Sprout Icon, Big Digits, Status Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics {
                contentDescription = "$statusLabel: $formattedTime"
            },
        ) {
            FocusSproutIcon(
                modifier = Modifier.size(32.dp),
                tint = FocusForestGreen,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formattedTime,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = TimerFontFamily,
                color = FocusDarkText,
                letterSpacing = (-1).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = statusLabel,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = FocusMutedText,
            )
        }
    }
}
