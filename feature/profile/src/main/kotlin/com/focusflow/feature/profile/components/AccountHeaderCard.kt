package com.focusflow.feature.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusflow.core.designsystem.component.FocusFlowActionButton
import com.focusflow.core.designsystem.component.FocusFlowChipBadge
import com.focusflow.core.designsystem.component.FocusSproutIcon
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusTertiary
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.feature.profile.R

@Composable
fun AccountHeaderCard(
    account: UserAccount?,
    isSyncing: Boolean,
    onSyncClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAnonymous = account?.isAnonymous != false
    val name = account?.displayName
        ?: if (isAnonymous) stringResource(R.string.profile_account_guest_name)
        else stringResource(R.string.profile_account_default_name)
    val email = account?.email ?: stringResource(R.string.profile_account_guest_email)

    ProfileSectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(isAnonymous = isAnonymous)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusDarkText,
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusMutedText,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isAnonymous) {
                    FocusFlowChipBadge(
                        text = stringResource(R.string.profile_account_badge_guest),
                        icon = Icons.Outlined.Person,
                    )
                } else {
                    FocusFlowChipBadge(
                        text = stringResource(R.string.profile_account_badge_signed_in),
                        icon = Icons.Filled.CheckCircle,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            SyncActionButton(
                isSyncing = isSyncing,
                onSyncClicked = onSyncClicked,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    isAnonymous: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(62.dp)) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(FocusForestMintBg),
            contentAlignment = Alignment.Center,
        ) {
            FocusSproutIcon(
                modifier = Modifier.size(32.dp),
                tint = FocusForestGreen,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp)
                .clip(CircleShape)
                .background(if (isAnonymous) FocusTertiary else FocusForestGreen),
        )
    }
}

@Composable
private fun SyncActionButton(
    isSyncing: Boolean,
    onSyncClicked: () -> Unit,
) {
    if (isSyncing) {
        Surface(
            shape = CircleShape,
            color = FocusForestMintBg,
            border = BorderStroke(1.dp, FocusCardBorder),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = FocusForestGreenDeep,
                )
            }
        }
    } else {
        FocusFlowActionButton(
            icon = Icons.Filled.Sync,
            onClick = onSyncClicked,
            contentDescription = stringResource(R.string.profile_account_cd_sync),
        )
    }
}
