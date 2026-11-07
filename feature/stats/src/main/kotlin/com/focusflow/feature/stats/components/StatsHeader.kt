package com.focusflow.feature.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.feature.stats.R

/**
 * Top header row featuring "Your focus" serif title, contextual subtitle,
 * and a rounded calendar button.
 */
@Composable
fun StatsHeader(
    onCalendarClick: () -> Unit,
    onRefreshClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val calendarCd = stringResource(R.string.stats_cd_calendar)
    val refreshCd = stringResource(R.string.stats_cd_refresh)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.stats_screen_title),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = FocusDarkText,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.semantics { heading() },
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Live Sync / Refresh Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, FocusCardBorder),
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onRefreshClick,
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = refreshCd
                    },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = FocusDarkText,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Calendar / Jump to Today Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, FocusCardBorder),
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onCalendarClick,
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = calendarCd
                    },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = FocusDarkText,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
