package com.focusflow.feature.stats.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.AppUsageItem
import com.focusflow.feature.stats.R

/**
 * Modernized top used apps card matching the nature/forest design system.
 */
@Composable
fun TopAppsCard(
    apps: List<AppUsageItem>,
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
            Text(
                text = stringResource(R.string.stats_top_apps_title),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = FocusDarkText,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_top_apps_empty),
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = FocusMutedText,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                apps.forEachIndexed { index, item ->
                    AppRow(item = item)
                    if (index < apps.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    item: AppUsageItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                // App Initial Squircle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FocusForestMintBg,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.appName.take(1).uppercase(),
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FocusForestGreenDeep,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.appName,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusDarkText,
                    )
                    if (item.isSafeListed) {
                        Text(
                            text = stringResource(R.string.stats_app_safelisted),
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = FocusForestGreen,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Text(
                text = item.formattedDuration,
                fontFamily = OutfitFontFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FocusDarkText,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Custom Progress Track & Fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF5F1)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.fractionOfTotal.coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(FocusForestGreen),
            )
        }
    }
}
