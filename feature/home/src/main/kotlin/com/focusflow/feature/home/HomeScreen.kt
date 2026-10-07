package com.focusflow.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestAccentWave
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestGreenLight
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusInactiveIcon
import com.focusflow.core.designsystem.theme.FocusMutedText

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
    onNavigateToFocus: () -> Unit = {},
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onNavigateToFocus = onNavigateToFocus,
        onNavigateToLimits = onNavigateToLimits,
        onNavigateToSettings = onNavigateToSettings,
        onSelectMode = viewModel::selectMode,
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
    onNavigateToFocus: () -> Unit = {},
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSelectMode: (FocusMode) -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // ── 1. Scenic Nature Background Image ────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.bg_home_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── 2. Scrollable / Adaptive Screen Content ──────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top Bar: Logo & Settings ─────────────────────────────────────
            HomeTopBar(
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )

            // ── Headline: "Stay Focused, Build a Better You" ─────────────────
            HomeHeadlineSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Center Focus Action: Concentric Glowing Play Button ──────────
            CentralFocusButton(
                onClick = onNavigateToFocus,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(34.dp))

            // ── Focus Mode Card: "Work", "Study", "Personal" ─────────────────
            FocusModeCard(
                selectedMode = uiState.selectedMode,
                onSelectMode = onSelectMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

/**
 * Top branding bar with "FocusFlow" logo and tagline on the left,
 * and a subtle circular Settings icon button on the right.
 */
@Composable
private fun HomeTopBar(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsCd = stringResource(R.string.home_cd_settings)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_brand_focus),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusDarkText,
                )
                Text(
                    text = stringResource(R.string.home_brand_flow),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreen,
                )
            }
            Text(
                text = stringResource(R.string.home_tagline),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = FocusMutedText,
            )
        }

        // Settings gear button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xD9FFFFFF))
                .border(BorderStroke(1.dp, Color(0x22000000)), CircleShape)
                .clickable(
                    indication = ripple(bounded = true, radius = 22.dp),
                    interactionSource = null,
                    onClickLabel = settingsCd,
                    onClick = onNavigateToSettings,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = settingsCd
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = FocusDarkText,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Hero headline with styled "Stay Focused, Build a Better You"
 * including a custom organic brush-drawn wavy underline beneath "Better You".
 */
@Composable
private fun HomeHeadlineSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Stay Focused,",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FocusDarkText,
            lineHeight = 36.sp,
            modifier = Modifier.semantics { heading() },
        )

        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Build a ",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
                lineHeight = 36.sp,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_headline_better_you),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                    lineHeight = 36.sp,
                )
                WavyAccentUnderline(
                    modifier = Modifier
                        .width(148.dp)
                        .height(8.dp)
                        .offset(y = (-2).dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.home_headline_subtitle),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = FocusMutedText,
            lineHeight = 22.sp,
        )
    }
}

/**
 * Hand-drawn style wavy underline accent curve under "Better You".
 */
@Composable
private fun WavyAccentUnderline(
    modifier: Modifier = Modifier,
    color: Color = FocusForestAccentWave,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(x = 0f, y = height * 0.7f)
            cubicTo(
                x1 = width * 0.22f, y1 = height * 0.15f,
                x2 = width * 0.44f, y2 = height * 0.95f,
                x3 = width * 0.70f, y3 = height * 0.35f,
            )
            quadraticTo(
                x1 = width * 0.88f, y1 = height * 0.10f,
                x2 = width, y2 = height * 0.50f,
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 3.2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Concentric glowing circular rings surrounding the central Play button,
 * with label "Start Focus Session" beneath.
 */
@Composable
private fun CentralFocusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startFocusActionLabel = stringResource(R.string.home_action_start_focus)
    val startFocusCd = stringResource(R.string.home_cd_start_focus)

    // Gentle ambient pulse for the halo rings
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Outer Halo Ring
            Box(
                modifier = Modifier
                    .size(206.dp * pulseScale)
                    .clip(CircleShape)
                    .background(Color(0x1F286D4D)),
            )

            // Middle Halo Ring
            Box(
                modifier = Modifier
                    .size(164.dp * pulseScale)
                    .clip(CircleShape)
                    .background(Color(0x2E286D4D)),
            )

            // Inner Halo Ring
            Box(
                modifier = Modifier
                    .size(132.dp * pulseScale)
                    .clip(CircleShape)
                    .background(Color(0x42286D4D)),
            )

            // Central Play Button
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x601B4E36),
                        spotColor = Color(0x801B4E36),
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FocusForestGreenLight,
                                FocusForestGreen,
                                FocusForestGreenDeep,
                            ),
                        ),
                    )
                    .clickable(
                        indication = ripple(bounded = true, radius = 53.dp),
                        interactionSource = null,
                        onClickLabel = startFocusActionLabel,
                        onClick = onClick,
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = startFocusCd
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(46.dp)
                        .offset(x = 3.dp), // Optical alignment for play triangle
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = startFocusActionLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = FocusDarkText,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Focus Mode Card container featuring 3 mode choices: "Work", "Study", "Personal".
 */
@Composable
private fun FocusModeCard(
    selectedMode: FocusMode,
    onSelectMode: (FocusMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x0D000000),
                spotColor = Color(0x12000000),
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(R.string.home_section_focus_mode),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )
            Text(
                text = stringResource(R.string.home_section_choose_mode),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = FocusMutedText,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FocusModeItem(
                    title = stringResource(R.string.home_mode_work_title),
                    subtitle = stringResource(R.string.home_mode_work_subtitle),
                    icon = Icons.Outlined.Laptop,
                    isSelected = selectedMode == FocusMode.WORK,
                    onClick = { onSelectMode(FocusMode.WORK) },
                    modifier = Modifier.weight(1f),
                )

                FocusModeItem(
                    title = stringResource(R.string.home_mode_study_title),
                    subtitle = stringResource(R.string.home_mode_study_subtitle),
                    icon = Icons.Outlined.School,
                    isSelected = selectedMode == FocusMode.STUDY,
                    onClick = { onSelectMode(FocusMode.STUDY) },
                    modifier = Modifier.weight(1f),
                )

                FocusModeItem(
                    title = stringResource(R.string.home_mode_personal_title),
                    subtitle = stringResource(R.string.home_mode_personal_subtitle),
                    icon = Icons.Outlined.Person,
                    isSelected = selectedMode == FocusMode.PERSONAL,
                    onClick = { onSelectMode(FocusMode.PERSONAL) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Individual mode tile inside the Focus Mode Card.
 */
@Composable
private fun FocusModeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBg else Color(0xFFFAFBFA),
        label = "mode_bg",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBorder else Color(0xFFE5ECE8),
        label = "mode_border",
    )
    val badgeBg = if (isSelected) FocusForestMintBadge else Color(0xFFEFF3F1)
    val iconTint = if (isSelected) FocusForestGreenDeep else FocusInactiveIcon
    val titleColor = if (isSelected) FocusForestGreenDeep else FocusDarkText
    val subtitleColor = if (isSelected) FocusForestGreen else FocusMutedText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBg)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 1.6.dp else 1.dp,
                    color = animatedBorderColor,
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 14.dp)
            .semantics {
                role = Role.RadioButton
                this.contentDescription = "$title, $subtitle"
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = titleColor,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2,
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

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
