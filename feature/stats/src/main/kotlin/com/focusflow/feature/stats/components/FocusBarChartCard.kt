package com.focusflow.feature.stats.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.ChartBarItem
import com.focusflow.feature.stats.R

// Amber/orange tone for the goal dashed guideline
private val GoalLineColor = Color(0xFFD97724)
// Soft sage green for bars below the goal
private val BelowGoalBarColor = Color(0xFF9DC4B2)

/**
 * Focus bar chart card replicating the reference mockup:
 * Shows title "Focus each day", custom dashed horizontal goal line,
 * rounded bars (dark forest green for goal reached, soft sage green for below),
 * value text on top, day labels on bottom, and a legend row.
 */
@Composable
fun FocusBarChartCard(
    chartTitle: String,
    bars: List<ChartBarItem>,
    dailyGoalHours: Float,
    dailyGoalFormatted: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            // Card Title: "Focus each day"
            Text(
                text = chartTitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = FocusDarkText,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Chart area with dashed goal line
            if (bars.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                ) {
                    Text(
                        text = "No focus data recorded yet",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        color = FocusMutedText,
                    )
                }
            } else {
                val maxHours = maxOf(
                    dailyGoalHours * 1.35f,
                    bars.maxOfOrNull { it.hours }?.takeIf { it > 0f }?.let { it * 1.25f } ?: 3.5f,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                ) {
                    // Dashed Guideline Canvas
                    val goalFraction = (dailyGoalHours / maxHours).coerceIn(0.1f, 0.9f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp, bottom = 24.dp), // align with bar drawing area
                    ) {
                        val y = size.height * (1f - goalFraction)
                        drawLine(
                            color = GoalLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                            cap = StrokeCap.Round,
                        )
                    }

                    // Columns row
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        bars.forEach { bar ->
                            ChartColumn(
                                bar = bar,
                                maxHours = maxHours,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Item 1: Goal reached dot
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(FocusForestGreenDeep),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.stats_chart_legend_goal_reached),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FocusMutedText,
                )

                Spacer(modifier = Modifier.width(18.dp))

                // Item 2: Dashed goal line
                Canvas(modifier = Modifier.size(width = 16.dp, height = 3.dp)) {
                    drawLine(
                        color = GoalLineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f), 0f),
                        cap = StrokeCap.Round,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.stats_chart_legend_daily_goal, dailyGoalFormatted),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FocusMutedText,
                )
            }
        }
    }
}

@Composable
private fun ChartColumn(
    bar: ChartBarItem,
    maxHours: Float,
    modifier: Modifier = Modifier,
) {
    val heightFraction = if (maxHours > 0f) {
        (bar.hours / maxHours).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedHeightFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 650),
        label = "bar_height_${bar.id}",
    )

    val barColor = if (bar.isGoalReached) {
        FocusForestGreenDeep
    } else {
        BelowGoalBarColor
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .semantics(mergeDescendants = true) {
                contentDescription = "${bar.fullLabel}: ${bar.formattedDuration}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Value label above bar (e.g. "1.8h", "3h")
        Text(
            text = if (bar.hours > 0f) bar.formattedDuration else "",
            fontFamily = OutfitFontFamily,
            fontWeight = if (bar.isGoalReached) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.5.sp,
            color = if (bar.isGoalReached) FocusDarkText else FocusMutedText,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Bar Track & Fill
        Box(
            modifier = Modifier
                .height(115.dp)
                .width(26.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeightFraction.coerceAtLeast(0.04f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(barColor),
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Day label below bar (e.g. "Mon", "Tue", ..., "Sun")
        Text(
            text = bar.label,
            fontFamily = OutfitFontFamily,
            fontWeight = if (bar.isCurrent || bar.isGoalReached) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            color = if (bar.isCurrent) FocusDarkText else FocusMutedText,
            textAlign = TextAlign.Center,
        )
    }
}
