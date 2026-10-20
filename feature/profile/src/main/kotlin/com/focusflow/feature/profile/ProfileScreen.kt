package com.focusflow.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.component.FocusFlowAppBar
import com.focusflow.core.designsystem.component.FocusFlowLogo
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusScreenBackground
import com.focusflow.feature.profile.components.AccountHeaderCard
import com.focusflow.feature.profile.components.DataExportDialog
import com.focusflow.feature.profile.components.DataManagementCard
import com.focusflow.feature.profile.components.DeleteAccountConfirmDialog
import com.focusflow.feature.profile.components.GamificationSummaryCard
import com.focusflow.feature.profile.components.PrivacyDashboardCard
import com.focusflow.feature.profile.components.ProfileToolsCard

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onNavigateToLimits: () -> Unit = {},
    onNavigateToPermissionHealth: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onClearMessage()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = ProfileActions(
            onSync = viewModel::onSyncClicked,
            onExport = viewModel::onExportDataClicked,
            onSignOut = viewModel::onSignOutClicked,
            onDeleteRequested = { showDeleteDialog = true },
            onNavigateToLimits = onNavigateToLimits,
            onNavigateToPermissionHealth = onNavigateToPermissionHealth,
        ),
        modifier = modifier,
    )

    if (showDeleteDialog) {
        DeleteAccountConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteAccountClicked()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    uiState.exportedDataJson?.let { json ->
        DataExportDialog(
            jsonContent = json,
            onDismiss = viewModel::onDismissExport,
        )
    }
}

/** Stateless content composable for the Profile screen. */
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    actions: ProfileActions,
    onBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            FocusFlowAppBar(
                title = stringResource(R.string.profile_title),
                subtitle = stringResource(R.string.profile_subtitle),
                onBack = onBack,
                containerColor = FocusScreenBackground,
            )
        },
        containerColor = FocusScreenBackground,
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AccountHeaderCard(
                account = uiState.account,
                isSyncing = uiState.isSyncing,
                onSyncClicked = actions.onSync,
            )

            GamificationSummaryCard(profile = uiState.profile)

            ProfileToolsCard(
                onNavigateToLimits = actions.onNavigateToLimits,
                onNavigateToPermissionHealth = actions.onNavigateToPermissionHealth,
            )

            PrivacyDashboardCard()

            DataManagementCard(
                isExporting = uiState.isExporting,
                onExportClicked = actions.onExport,
                onSignOutClicked = actions.onSignOut,
                onDeleteAccountRequested = actions.onDeleteRequested,
            )

            ProfileFooter()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FocusFlowLogo(fontSize = 20.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.profile_footer_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = FocusMutedText,
            textAlign = TextAlign.Center,
        )
    }
}
