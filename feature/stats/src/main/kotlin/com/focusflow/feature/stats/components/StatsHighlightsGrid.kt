package com.focusflow.feature.stats.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusPrimary
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.R
import com.focusflow.feature.stats.StatsHighlights

/**
 * Refined 2x2 Highlights grid replicating the reference mockup:
 * Shows best day, peak focus window, avg session duration, and goal streak.
 */
@Composable
fun StatsHighlightsGrid(
    highlights: StatsHighlights,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Section Header
        Text(
            text = stringResource(R.string.stats_highlights_title),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = FocusDarkText,
            modifier = Modifier.semantics { heading() },
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Best Day & Best Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HighlightCard(
                icon = Icons.Outlined.StarOutline,
                iconTint = FocusForestGreenDeep,
                badgeBg = Color(0xFFE8F4EC),
                title = highlights.bestDayName,
                caption = highlights.bestDayCaption,
                modifier = Modifier.weight(1f),
            )

            HighlightCard(
                icon = Icons.Outlined.AccessTime,
                iconTint = FocusPrimary,
                badgeBg = Color(0xFFEEF3FF),
                title = highlights.bestTimeWindow,
                caption = highlights.bestTimeCaption,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Avg Session & Goal Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HighlightCard(
                icon = Icons.Outlined.Timer,
                iconTint = FocusForestGreenDeep,
                badgeBg = Color(0xFFE8F4EC),
                title = highlights.avgSessionLength,
                caption = highlights.avgSessionCaption,
                modifier = Modifier.weight(1f),
            )

            HighlightCard(
                icon = Icons.Outlined.LocalFireDepartment,
                iconTint = Color(0xFFE67E22),
                badgeBg = Color(0xFFFFF3E6),
                title = highlights.streakCount,
                caption = highlights.streakCaption,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HighlightCard(
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Color,
    title: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeBg,
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FocusDarkText,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = caption,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = FocusMutedText,
                    maxLines = 1,
                )
            }
        }
    }
}
