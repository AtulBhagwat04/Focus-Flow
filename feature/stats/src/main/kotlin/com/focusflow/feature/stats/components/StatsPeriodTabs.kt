package com.focusflow.feature.stats.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.R
import com.focusflow.feature.stats.StatsTimeRange

/**
 * Refined period selector tabs (Week and Month) with a sleek bottom indicator
 * flush with a subtle baseline divider, matching the reference mockup.
 */
@Composable
fun StatsPeriodTabs(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        StatsTimeRange.WEEK to stringResource(R.string.stats_tab_week),
        StatsTimeRange.MONTH to stringResource(R.string.stats_tab_month),
    )

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Subtle full-width baseline divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(FocusCardBorder),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (range, label) ->
                val isSelected = range == selectedRange
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) FocusDarkText else FocusMutedText,
                    label = "tab_text_color",
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { onRangeSelected(range) },
                        )
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                        }
                        .padding(top = 6.dp),
                ) {
                    Text(
                        text = label,
                        fontFamily = OutfitFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.5.sp,
                        color = textColor,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Tab Underline Indicator
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(
                                if (isSelected) FocusForestGreenDeep else Color.Transparent,
                            ),
                    )
                }
            }
        }
    }
}
