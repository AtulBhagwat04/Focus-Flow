package com.focusflow.feature.home.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.home.R
import kotlinx.coroutines.launch

/**
 * Stateful Login screen composable.
 *
 * Implements Google Sign-In and anonymous fallback matching the design reference.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthHelper = remember(context) { GoogleAuthHelper(context) }

    val serverClientId = remember(context) {
        val directId = runCatching { context.getString(R.string.google_web_client_id) }.getOrNull()
        if (!directId.isNullOrBlank()) {
            directId
        } else {
            val packageName = context.packageName
            val resId = listOf(
                context.resources.getIdentifier("default_web_client_id", "string", packageName),
                context.resources.getIdentifier("default_web_client_id", "string", "com.focusflow.app"),
            ).firstOrNull { it != 0 } ?: 0

            if (resId != 0) context.getString(resId) else ""
        }
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateForward -> onLoginSuccess()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Continue as Guest",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onGuestSignIn()
            }
            viewModel.onDismissError()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onContinueWithGoogle = {
            viewModel.onGoogleSignInStarted()
            scope.launch {
                val tokenResult = googleAuthHelper.getGoogleIdToken(serverClientId)
                tokenResult.fold(
                    onSuccess = { idToken ->
                        viewModel.onGoogleIdTokenReceived(idToken)
                    },
                    onFailure = { error ->
                        viewModel.onGoogleSignInError(error)
                    }
                )
            }
        },
        onExploreAsGuest = viewModel::onGuestSignIn,
        onShowTerms = viewModel::onShowTerms,
        onDismissTerms = viewModel::onDismissTerms,
        onShowPrivacy = viewModel::onShowPrivacy,
        onDismissPrivacy = viewModel::onDismissPrivacy,
        modifier = modifier,
    )
}

/**
 * Stateless Login screen UI matching the scenic nature reference layout.
 */
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onContinueWithGoogle: () -> Unit,
    onExploreAsGuest: () -> Unit = {},
    onShowTerms: () -> Unit,
    onDismissTerms: () -> Unit,
    onShowPrivacy: () -> Unit,
    onDismissPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // ── 1. Full Screen Scenic Nature Background ──────────────────────────
        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── 2. Soft Frosted White Fade at Bottom ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.30f to Color(0x66FFFFFF),
                        0.60f to Color(0xE6FFFFFF),
                        0.82f to Color.White,
                        1.0f to Color.White,
                    )
                )
        )

        // ── 3. Foreground Content ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(104.dp))

            // ── FocusFlow Brand Title & Wave Accent ──────────────────────────
            FocusFlowBrandingHeader()

            Spacer(modifier = Modifier.weight(1f))

            // ── Bottom Action Area ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // "Continue with Google" Pill Button
                GoogleSignInButton(
                    isLoading = uiState.isGoogleLoading,
                    enabled = !uiState.isGoogleLoading && !uiState.isGuestLoading,
                    onClick = onContinueWithGoogle,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Legal Disclaimer & Links
                LegalDisclaimerSection(
                    onTermsClick = onShowTerms,
                    onPrivacyClick = onShowPrivacy,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Snackbar Host for quiet error messaging
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
        )
    }

    // Terms of Service Dialog
    if (uiState.showTermsDialog) {
        AlertDialog(
            onDismissRequest = onDismissTerms,
            title = {
                Text(
                    text = stringResource(R.string.login_terms_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.login_terms_dialog_body),
                    color = FocusDarkText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissTerms) {
                    Text(
                        text = stringResource(R.string.login_dialog_close),
                        color = FocusForestGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }

    // Privacy Policy Dialog
    if (uiState.showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = onDismissPrivacy,
            title = {
                Text(
                    text = stringResource(R.string.login_privacy_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.login_privacy_dialog_body),
                    color = FocusDarkText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissPrivacy) {
                    Text(
                        text = stringResource(R.string.login_dialog_close),
                        color = FocusForestGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }
}

/**
 * Branded FocusFlow Logo Header with distinctive wave underline.
 */
@Composable
private fun FocusFlowBrandingHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.home_brand_focus),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(bottom = 9.dp), // Aligns baseline with "Flow"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Max),
            ) {
                Text(
                    text = stringResource(R.string.home_brand_flow),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreen,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                FocusFlowWaveAccent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.home_tagline),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = FocusMutedText,
            letterSpacing = 0.2.sp,
        )
    }
}

/**
 * Curved wave underline drawn under "Flow" matching the design system.
 */
@Composable
fun FocusFlowWaveAccent(
    modifier: Modifier = Modifier,
    color: Color = FocusForestGreen,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h * 0.45f)
            // Left half: smooth gentle trough dipping under 'l' and 'o'
            cubicTo(
                w * 0.18f, h * 0.98f,
                w * 0.38f, h * 0.98f,
                w * 0.52f, h * 0.48f,
            )
            // Right half: gentle crest rising under 'w' and returning to center
            cubicTo(
                w * 0.66f, 0f,
                w * 0.86f, 0f,
                w, h * 0.42f,
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.8.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )
    }
}

/**
 * High-fidelity "Continue with Google" pill button matching the reference mockup.
 */
@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, FocusCardBorder),
        modifier = modifier
            .height(58.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x1F000000),
            )
            .semantics {
                role = Role.Button
                contentDescription = "Continue with Google"
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = FocusForestGreen,
                    strokeWidth = 2.5.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.login_signing_in),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusDarkText,
                )
            } else {
                Icon(
                    painter = painterResource(id = com.focusflow.core.designsystem.R.drawable.ic_google_logo),
                    contentDescription = null,
                    tint = Color.Unspecified, // Retains genuine Google 4 colors
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.login_continue_with_google),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusDarkText,
                )
            }
        }
    }
}

/**
 * Clickable legal terms and privacy disclaimer.
 */
@Composable
private fun LegalDisclaimerSection(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.login_terms_prefix),
            fontSize = 12.sp,
            color = FocusMutedText,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.login_terms_of_service),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FocusForestGreen,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onTermsClick)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            )
            Text(
                text = stringResource(R.string.login_and),
                fontSize = 12.sp,
                color = FocusMutedText,
            )
            Text(
                text = stringResource(R.string.login_privacy_policy),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FocusForestGreen,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onPrivacyClick)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    FocusFlowTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onContinueWithGoogle = {},
            onExploreAsGuest = {},
            onShowTerms = {},
            onDismissTerms = {},
            onShowPrivacy = {},
            onDismissPrivacy = {},
        )
    }
}
