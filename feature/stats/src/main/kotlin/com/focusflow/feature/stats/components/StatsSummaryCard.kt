package com.focusflow.feature.stats.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.OutfitFontFamily

/**
 * Summary card matching the reference mockup:
 * "Focused this week", large serif time display, trend comparison message,
 * and goal progress bar with remaining time.
 */
@Composable
fun StatsSummaryCard(
    heroLabel: String,
    totalFocusTime: String,
    trendText: String,
    isTrendPositive: Boolean,
    goalLabel: String,
    goalRemainingText: String,
    goalProgress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goalProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "summary_goal_progress",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F3EC),
        ),
        border = BorderStroke(1.dp, Color(0xFFD6E8DC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            // Label: "Focused this week"
            Text(
                text = heroLabel,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF385244),
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Large Focus Time: "12h 30m"
            Text(
                text = totalFocusTime,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = FocusDarkText,
                letterSpacing = (-1).sp,
                lineHeight = 42.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Trend comparison row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = if (isTrendPositive) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isTrendPositive) FocusForestGreenDeep else Color(0xFF7A6B32),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = trendText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF1E4D38),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Goal Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = goalLabel,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = FocusDarkText,
                )
                Text(
                    text = goalRemainingText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = Color(0xFF476054),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Custom Smooth Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(FocusForestGreenDeep),
                )
            }
        }
    }
}
