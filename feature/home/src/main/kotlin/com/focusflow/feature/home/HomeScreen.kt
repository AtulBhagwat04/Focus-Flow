package com.focusflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusFlowTheme

/**
 * Home screen entry point.
 *
 * Follows Unidirectional Data Flow:
 * - Collects [HomeUiState] lifecycle-aware via [collectAsStateWithLifecycle]
 * - Delegates rendering to stateless [HomeScreenContent]
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState  = uiState,
        modifier = modifier,
    )
}

/**
 * Stateless content composable for Home screen.
 * Hoists state for easy Compose UI testing and @Preview support.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector         = Icons.Outlined.Timer,
            contentDescription  = null,
            modifier            = Modifier.size(64.dp),
            tint                = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text      = uiState.title,
            style     = MaterialTheme.typography.headlineMedium,
            color     = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier  = Modifier.semantics { heading() },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text      = uiState.subtitle,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, name = "Home — Light")
@Composable
private fun HomeScreenLightPreview() {
    FocusFlowTheme(darkTheme = false) {
        HomeScreenContent(uiState = HomeUiState())
    }
}

@Preview(showBackground = true, name = "Home — Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    FocusFlowTheme(darkTheme = true) {
        HomeScreenContent(uiState = HomeUiState())
    }
}
