package com.focusflow.feature.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.profile.R

@Composable
fun PrivacyDashboardCard(
    modifier: Modifier = Modifier,
) {
    ProfileSectionCard(modifier = modifier) {
        ProfileSectionHeader(
            icon = Icons.Filled.Lock,
            title = stringResource(R.string.profile_privacy_title),
            subtitle = stringResource(R.string.profile_privacy_subtitle),
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        PrivacySectionLabel(
            text = stringResource(R.string.profile_privacy_section_local),
            color = FocusForestGreen,
        )

        Spacer(modifier = Modifier.height(6.dp))

        PrivacyItem(
            title = stringResource(R.string.profile_privacy_item_usage_title),
            detail = stringResource(R.string.profile_privacy_item_usage_detail),
        )
        PrivacyItem(
            title = stringResource(R.string.profile_privacy_item_rules_title),
            detail = stringResource(R.string.profile_privacy_item_rules_detail),
        )
        PrivacyItem(
            title = stringResource(R.string.profile_privacy_item_content_title),
            detail = stringResource(R.string.profile_privacy_item_content_detail),
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        PrivacySectionLabel(
            text = stringResource(R.string.profile_privacy_section_cloud),
            color = FocusForestGreenDeep,
        )

        Spacer(modifier = Modifier.height(6.dp))

        PrivacyItem(
            title = stringResource(R.string.profile_privacy_item_xp_title),
            detail = stringResource(R.string.profile_privacy_item_xp_detail),
            tint = FocusForestGreenDeep,
        )
    }
}

@Composable
private fun PrivacySectionLabel(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = color,
    )
}

@Composable
private fun PrivacyItem(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    tint: Color = FocusForestGreen,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = FocusDarkText,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )
        }
    }
}
