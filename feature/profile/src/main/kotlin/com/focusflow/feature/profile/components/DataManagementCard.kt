package com.focusflow.feature.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusflow.core.designsystem.theme.FocusError
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.feature.profile.R

private val ProfileButtonShape = RoundedCornerShape(14.dp)

@Composable
fun DataManagementCard(
    isExporting: Boolean,
    onExportClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteAccountRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSectionCard(modifier = modifier) {
        ProfileSectionHeader(
            icon = Icons.Outlined.Storage,
            title = stringResource(R.string.profile_data_title),
            subtitle = stringResource(R.string.profile_data_subtitle),
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExportDataButton(
            isExporting = isExporting,
            onExportClicked = onExportClicked,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SignOutButton(onSignOutClicked = onSignOutClicked)

        Spacer(modifier = Modifier.height(4.dp))

        DeleteAccountButton(onDeleteAccountRequested = onDeleteAccountRequested)
    }
}

@Composable
private fun ExportDataButton(
    isExporting: Boolean,
    onExportClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onExportClicked,
        enabled = !isExporting,
        shape = ProfileButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FocusForestGreenDeep,
            contentColor = Color.White,
            disabledContainerColor = FocusForestMintBadge,
            disabledContentColor = FocusForestGreenDeep,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = FocusForestGreenDeep,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.profile_data_exporting))
        } else {
            Icon(
                imageVector = Icons.Filled.FileDownload,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.profile_data_export))
        }
    }
}

@Composable
private fun SignOutButton(
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onSignOutClicked,
        shape = ProfileButtonShape,
        border = BorderStroke(1.dp, FocusForestMintBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FocusForestGreenDeep,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.profile_data_sign_out))
    }
}

@Composable
private fun DeleteAccountButton(
    onDeleteAccountRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onDeleteAccountRequested,
        colors = ButtonDefaults.textButtonColors(contentColor = FocusError),
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.profile_data_delete))
    }
}
