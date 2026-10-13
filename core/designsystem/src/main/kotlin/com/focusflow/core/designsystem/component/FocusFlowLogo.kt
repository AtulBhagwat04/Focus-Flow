package com.focusflow.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen

/**
 * Curved wave underline drawn under "Flow" matching the FocusFlow brand design.
 */
@Composable
fun FocusFlowWaveAccent(
    modifier: Modifier = Modifier,
    color: Color = FocusForestGreen,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h * 0.45f)
            // Left half: smooth gentle trough dipping under 'l' and 'o'
            cubicTo(
                w * 0.18f, h * 0.98f,
                w * 0.38f, h * 0.98f,
                w * 0.52f, h * 0.48f,
            )
            // Right half: gentle crest rising under 'w' and returning to center
            cubicTo(
                w * 0.66f, 0f,
                w * 0.86f, 0f,
                w, h * 0.42f,
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )
    }
}

/**
 * Branded FocusFlow Logo with distinctive wavy underline accent under "Flow".
 */
@Composable
fun FocusFlowLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "Focus",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = FocusDarkText,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(bottom = (fontSize.value * 0.12f).dp),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max),
        ) {
            Text(
                text = "Flow",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = FocusForestGreen,
                letterSpacing = (-0.5).sp,
            )
            Spacer(modifier = Modifier.height(1.dp))
            FocusFlowWaveAccent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((fontSize.value * 0.18f).dp),
            )
        }
    }
}

/**
 * Sprout / seedling twin-leaf icon drawn with fine Bezier curves.
 */
@Composable
fun FocusSproutIcon(
    modifier: Modifier = Modifier,
    tint: Color = FocusForestGreen,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val stemY = h * 0.84f

        // Draw left leaf
        val leftLeaf = Path().apply {
            moveTo(centerX, stemY)
            cubicTo(
                centerX - w * 0.46f, stemY - h * 0.12f,
                centerX - w * 0.55f, stemY - h * 0.62f,
                centerX - w * 0.38f, stemY - h * 0.82f,
            )
            cubicTo(
                centerX - w * 0.14f, stemY - h * 0.78f,
                centerX - w * 0.04f, stemY - h * 0.44f,
                centerX, stemY,
            )
            close()
        }
        drawPath(path = leftLeaf, color = tint)

        // Draw right leaf
        val rightLeaf = Path().apply {
            moveTo(centerX, stemY)
            cubicTo(
                centerX + w * 0.46f, stemY - h * 0.12f,
                centerX + w * 0.55f, stemY - h * 0.62f,
                centerX + w * 0.38f, stemY - h * 0.82f,
            )
            cubicTo(
                centerX + w * 0.14f, stemY - h * 0.78f,
                centerX + w * 0.04f, stemY - h * 0.44f,
                centerX, stemY,
            )
            close()
        }
        drawPath(path = rightLeaf, color = tint)

        // Draw small stem base
        drawLine(
            color = tint,
            start = Offset(centerX, stemY),
            end = Offset(centerX, h),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
