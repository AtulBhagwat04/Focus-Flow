package com.focusflow.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focusflow.core.designsystem.R
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Botanical Custom Duration Dialog.
 *
 * Provides a time selection experience with direct numeric entry,
 * step increment/decrement arrows for hours and minutes, and botanical artwork.
 * Reused across both Home and Focus Timer screens.
 */
@Composable
fun CustomDurationDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    confirmButtonText: String? = null,
    modifier: Modifier = Modifier,
) {
    val initialHours = (currentMinutes / 60).coerceIn(0, 12)
    val initialMinutes = (currentMinutes % 60).coerceIn(0, 59)
    var hoursText by remember { mutableStateOf(if (currentMinutes <= 0) "00" else "%02d".format(initialHours)) }
    var minutesText by remember { mutableStateOf(if (currentMinutes <= 0) "30" else "%02d".format(initialMinutes)) }

    val hoursLabel = stringResource(R.string.core_custom_dialog_hours)
    val minutesLabel = stringResource(R.string.core_custom_dialog_minutes)
    val incHoursCd = stringResource(R.string.core_custom_dialog_inc_hours)
    val decHoursCd = stringResource(R.string.core_custom_dialog_dec_hours)
    val incMinutesCd = stringResource(R.string.core_custom_dialog_inc_minutes)
    val decMinutesCd = stringResource(R.string.core_custom_dialog_dec_minutes)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0x15000000),
                    spotColor = Color(0x25000000),
                ),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 1. Botanical Header with Clock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BotanicalLeafSprig(
                        modifier = Modifier.size(width = 46.dp, height = 36.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BotanicalClockBadge()
                    Spacer(modifier = Modifier.width(6.dp))
                    BotanicalLeafSprig(
                        modifier = Modifier
                            .size(width = 46.dp, height = 36.dp)
                            .graphicsLayer { scaleX = -1f },
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Title & Subtitle
                Text(
                    text = stringResource(R.string.core_custom_dialog_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF14291E),
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.core_custom_dialog_subtitle),
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.5.sp,
                    color = Color(0xFF5A6E65),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Time Steppers Container with manual entry
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3F7F4),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            // Hours Card with manual typing
                            TimeStepperBox(
                                valueText = hoursText,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    hoursText = if (digits.length > 2) digits.takeLast(2) else digits
                                },
                                onIncrement = {
                                    val current = hoursText.toIntOrNull() ?: 0
                                    val newH = (current + 1).coerceAtMost(12)
                                    hoursText = "%02d".format(newH)
                                },
                                onDecrement = {
                                    val current = hoursText.toIntOrNull() ?: 0
                                    val newH = (current - 1).coerceAtLeast(0)
                                    hoursText = "%02d".format(newH)
                                },
                                incCd = incHoursCd,
                                decCd = decHoursCd,
                                modifier = Modifier.weight(1f),
                            )

                            // Colon separator
                            Text(
                                text = ":",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF14291E),
                                modifier = Modifier.padding(horizontal = 10.dp),
                            )

                            // Minutes Card with manual typing
                            TimeStepperBox(
                                valueText = minutesText,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    minutesText = if (digits.length > 2) digits.takeLast(2) else digits
                                },
                                onIncrement = {
                                    val currentM = minutesText.toIntOrNull() ?: 0
                                    val currentH = hoursText.toIntOrNull() ?: 0
                                    val next = currentM + 5
                                    if (next >= 60) {
                                        minutesText = "00"
                                        if (currentH < 12) {
                                            hoursText = "%02d".format(currentH + 1)
                                        }
                                    } else {
                                        minutesText = "%02d".format(next)
                                    }
                                },
                                onDecrement = {
                                    val currentM = minutesText.toIntOrNull() ?: 0
                                    val currentH = hoursText.toIntOrNull() ?: 0
                                    if (currentM >= 5) {
                                        minutesText = "%02d".format(currentM - 5)
                                    } else if (currentH > 0) {
                                        hoursText = "%02d".format(currentH - 1)
                                        minutesText = "55"
                                    } else {
                                        minutesText = "05"
                                    }
                                },
                                incCd = incMinutesCd,
                                decCd = decMinutesCd,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Labels: "Hours" & "Minutes"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = hoursLabel,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = Color(0xFF5A6E65),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(modifier = Modifier.width(28.dp))

                            Text(
                                text = minutesLabel,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = Color(0xFF5A6E65),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Action Buttons (Cancel + Confirm)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel Button
                    Surface(
                        modifier = Modifier
                            .weight(0.40f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                indication = ripple(bounded = true),
                                interactionSource = null,
                                onClick = onDismiss,
                            )
                            .semantics { role = Role.Button },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.core_custom_dialog_cancel),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF1E293B),
                            )
                        }
                    }

                    // Confirm Button ("Start Focus" or "Set Duration")
                    val resolvedConfirmText = confirmButtonText
                        ?: stringResource(R.string.core_custom_dialog_confirm)

                    Surface(
                        modifier = Modifier
                            .weight(0.60f)
                            .height(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0x201B4E36),
                                spotColor = Color(0x351B4E36),
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                indication = ripple(bounded = true),
                                interactionSource = null,
                                onClick = {
                                    val h = (hoursText.toIntOrNull() ?: 0).coerceIn(0, 12)
                                    val m = (minutesText.toIntOrNull() ?: 0).coerceIn(0, 59)
                                    val totalMinutes = (h * 60 + m).coerceAtLeast(1)
                                    onConfirm(totalMinutes)
                                },
                            )
                            .semantics { role = Role.Button },
                        shape = RoundedCornerShape(24.dp),
                        color = FocusForestGreenDeep,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = resolvedConfirmText,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeStepperBox(
    valueText: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    incCd: String,
    decCd: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0F000000),
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2EBE5)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (valueText.isEmpty()) {
                    Text(
                        text = "00",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFFCBD5E1),
                    )
                }
                BasicTextField(
                    value = valueText,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF14291E),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(FocusForestGreenDeep),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = incCd,
                    tint = Color(0xFF14291E),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true, radius = 12.dp),
                            interactionSource = null,
                            onClick = onIncrement,
                        ),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = decCd,
                    tint = Color(0xFF14291E),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true, radius = 12.dp),
                            interactionSource = null,
                            onClick = onDecrement,
                        ),
                )
            }
        }
    }
}

@Composable
fun BotanicalClockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFEAF3ED)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val r = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val strokeW = 2.4.dp.toPx()
            val clockColor = Color(0xFF14291E)

            // Outer dial ring
            drawCircle(
                color = clockColor,
                radius = r - strokeW / 2,
                style = Stroke(width = strokeW),
            )

            // Center hub
            drawCircle(
                color = clockColor,
                radius = 2.2.dp.toPx(),
            )

            // Hour hand pointing straight up (12:00)
            drawLine(
                color = clockColor,
                start = Offset(cx, cy),
                end = Offset(cx, cy - r * 0.52f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )

            // Minute hand pointing to ~5:24
            val minuteAngleRad = Math.toRadians(70.0).toFloat()
            val minuteLen = r * 0.62f
            drawLine(
                color = clockColor,
                start = Offset(cx, cy),
                end = Offset(
                    cx + cos(minuteAngleRad) * minuteLen,
                    cy + sin(minuteAngleRad) * minuteLen,
                ),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun BotanicalLeafSprig(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Stem: gentle curve from right to left
        val stemPath = Path().apply {
            moveTo(w * 0.95f, h * 0.72f)
            quadraticTo(w * 0.55f, h * 0.62f, w * 0.15f, h * 0.45f)
        }
        drawPath(
            path = stemPath,
            color = Color(0xFF729A82),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
        )

        fun drawLeaf(
            baseX: Float, baseY: Float,
            tipX: Float, tipY: Float,
            width: Float,
            fillColor: Color,
        ) {
            val dx = tipX - baseX
            val dy = tipY - baseY
            val len = hypot(dx, dy).coerceAtLeast(1f)
            val nx = (-dy / len) * width
            val ny = (dx / len) * width

            val midX = baseX + dx * 0.45f
            val midY = baseY + dy * 0.45f

            val leafPath = Path().apply {
                moveTo(baseX, baseY)
                quadraticTo(midX + nx, midY + ny, tipX, tipY)
                quadraticTo(midX - nx, midY - ny, baseX, baseY)
                close()
            }
            drawPath(path = leafPath, color = fillColor)

            // Subtle center vein
            drawLine(
                color = fillColor.copy(alpha = 0.55f),
                start = Offset(baseX, baseY),
                end = Offset(baseX + dx * 0.8f, baseY + dy * 0.8f),
                strokeWidth = 0.8.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Leaf 1: Upper-right (near badge)
        drawLeaf(
            baseX = w * 0.82f, baseY = h * 0.65f,
            tipX = w * 0.72f, tipY = h * 0.18f,
            width = 6.dp.toPx(),
            fillColor = Color(0xFF8EAF99),
        )

        // Leaf 2: Lower-middle
        drawLeaf(
            baseX = w * 0.62f, baseY = h * 0.62f,
            tipX = w * 0.48f, tipY = h * 0.92f,
            width = 5.dp.toPx(),
            fillColor = Color(0xFF6B937A),
        )

        // Leaf 3: Upper-left
        drawLeaf(
            baseX = w * 0.46f, baseY = h * 0.54f,
            tipX = w * 0.28f, tipY = h * 0.12f,
            width = 6.5.dp.toPx(),
            fillColor = Color(0xFF98BDA3),
        )

        // Leaf 4: Terminal tip
        drawLeaf(
            baseX = w * 0.22f, baseY = h * 0.47f,
            tipX = w * 0.02f, tipY = h * 0.38f,
            width = 5.dp.toPx(),
            fillColor = Color(0xFF7BA48C),
        )
    }
}

@Preview(showBackground = true, name = "Custom Duration Dialog Preview")
@Composable
private fun CustomDurationDialogComponentPreview() {
    FocusFlowTheme(darkTheme = false) {
        CustomDurationDialog(
            currentMinutes = 45,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
