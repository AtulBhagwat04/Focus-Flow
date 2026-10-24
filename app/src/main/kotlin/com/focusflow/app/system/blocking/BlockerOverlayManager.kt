package com.focusflow.app.system.blocking

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.focusflow.app.R
import com.focusflow.core.common.time.Clock
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.domain.blocking.model.BlockReason
import com.focusflow.core.domain.blocking.model.BlockingDecision
import com.focusflow.core.domain.blocking.model.EmergencyPass
import com.focusflow.core.domain.blocking.repository.EmergencyPassRepository
import com.focusflow.core.domain.repository.FocusSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val OverlayBgColor = Color(0xFFF7F8F4)
private val RayAccentColor = Color(0xFF8FA89B)
private val BadgeRedColor = Color(0xFFE54343)
private val BadgeShadowColor = Color(0x60E54343)
private val HeadlineNameColor = Color(0xFF1B2B23)
private val ForestGreenPrimary = Color(0xFF1E4D38)
private val SubtitleTextColor = Color(0xFF4B6054)
private val PillBorderColor = Color(0xFFE2ECE4)
private val NotNowButtonBg = Color(0xE8F7FAF6)
private val NotNowBorderColor = Color(0xFFC4D7CC)
private val NotNowTextColor = Color(0xFF2E453A)
private val EmergencyPassColor = Color(0xFF5B7367)
private val GridFallbackColor = Color(0xFF8A9E93)

@Singleton
class BlockerOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: FocusSessionRepository,
    private val emergencyPassRepository: EmergencyPassRepository,
    private val clock: Clock,
) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private var overlayView: ComposeView? = null
    private var isShowing = false

    private var currentLifecycleOwner: OverlayLifecycleOwner? = null

    suspend fun showOverlay(
        packageName: String,
        decision: BlockingDecision.Block,
        onEmergencyPass: suspend () -> Unit,
    ) = withContext(Dispatchers.Main) {
        if (isShowing) return@withContext
        if (!Settings.canDrawOverlays(context) || windowManager == null) return@withContext

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val dummyLifecycleOwner = OverlayLifecycleOwner()
        dummyLifecycleOwner.start()
        currentLifecycleOwner = dummyLifecycleOwner

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(dummyLifecycleOwner)
            setViewTreeViewModelStoreOwner(dummyLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(dummyLifecycleOwner)
            setContent {
                FocusFlowTheme {
                    val currentSession by sessionRepository.currentSession.collectAsState(initial = null)
                    val emergencyPass by emergencyPassRepository.emergencyPass.collectAsState(
                        initial = EmergencyPass(),
                    )

                    var currentEpochMs by remember { mutableLongStateOf(clock.now()) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000L)
                            currentEpochMs = clock.now()
                        }
                    }

                    val remainingSessionDurationMs = remember(currentSession, currentEpochMs) {
                        currentSession?.remainingDurationMs(currentEpochMs)
                    }

                    val remainingEmergencyPasses = remember(emergencyPass) {
                        (emergencyPass.maxPassesPerDay - emergencyPass.passesUsedToday).coerceAtLeast(0)
                    }

                    BlockerOverlayContent(
                        packageName = packageName,
                        decision = decision,
                        remainingSessionDurationMs = remainingSessionDurationMs,
                        remainingEmergencyPasses = remainingEmergencyPasses,
                        onBackToFocus = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(
                                context.packageName,
                            )?.apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(homeIntent)
                            }
                            hideOverlay()
                        },
                        onNotNow = {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(homeIntent)
                            hideOverlay()
                        },
                        onEmergencyPass = {
                            dummyLifecycleOwner.performPass(onEmergencyPass)
                            hideOverlay()
                        },
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, layoutParams)
            overlayView = composeView
            isShowing = true
        } catch (_: Exception) {
            dummyLifecycleOwner.destroy()
            currentLifecycleOwner = null
            overlayView = null
            isShowing = false
        }
    }

    fun hideOverlay() {
        if (!isShowing || overlayView == null || windowManager == null) return
        try {
            windowManager.removeView(overlayView)
        } catch (_: Exception) {
            // Degrades safely
        } finally {
            currentLifecycleOwner?.destroy()
            currentLifecycleOwner = null
            overlayView = null
            isShowing = false
        }
    }

    private class OverlayLifecycleOwner :
        androidx.lifecycle.LifecycleOwner,
        ViewModelStoreOwner,
        SavedStateRegistryOwner {

        private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val viewModelStore: ViewModelStore get() = store
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun start() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun destroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        }

        fun performPass(action: suspend () -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                action()
            }
        }
    }
}

@Suppress("LongParameterList", "LongMethod")

@Composable
internal fun BlockerOverlayContent(
    packageName: String,
    decision: BlockingDecision.Block,
    onBackToFocus: () -> Unit,
    onNotNow: () -> Unit,
    onEmergencyPass: () -> Unit,
    modifier: Modifier = Modifier,
    remainingSessionDurationMs: Long? = null,
    remainingEmergencyPasses: Int = 2,
) {
    val context = LocalContext.current
    val packageManager = remember { context.packageManager }

    val appInfo = remember(packageName) {
        try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: Exception) {
            null
        }
    }

    val appName = remember(appInfo, packageName) {
        if (appInfo != null) {
            try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                packageName
            }
        } else {
            if (packageName == "com.android.launcher") {
                "System Launcher"
            } else {
                packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    val appIconBitmap = remember(appInfo) {
        if (appInfo != null) {
            try {
                val drawable = packageManager.getApplicationIcon(appInfo)
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
                drawable.toBitmap(width = width, height = height).asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val subtitle = remember(decision) {
        when (decision.reason) {
            BlockReason.IN_FOCUS_SESSION -> "This app is blocked during your focus session."
            BlockReason.TIME_LIMIT_EXCEEDED,
            BlockReason.LAUNCH_LIMIT_EXCEEDED -> "You've reached your daily limit for this app."
            BlockReason.ACTIVE_SCHEDULE -> "This app is blocked by your active schedule."
            BlockReason.ALWAYS_BLOCKED -> "This app is configured as always blocked."
            BlockReason.SHORT_FORM_FEED -> "Short-form feeds are blocked to protect your focus."
            BlockReason.STUDY_MODE_CHANNEL_NOT_ALLOWLISTED -> "This channel is blocked during study mode."
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OverlayBgColor),
    ) {
        // ── 1. Serene Botanical & Landscape Artwork Background ─────────────────
        Image(
            painter = painterResource(id = R.drawable.bg_blocker_overlay),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // ── 2. Content Column ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BlockerHeroContent(
                appName = appName,
                appIconBitmap = appIconBitmap,
                subtitle = subtitle,
                reason = decision.reason,
                remainingSessionDurationMs = remainingSessionDurationMs,
                modifier = Modifier.weight(1f),
            )

            // ── Action Buttons: Anchored at the bottom (center) ───────────────
            BlockerActionButtons(
                remainingEmergencyPasses = remainingEmergencyPasses,
                onBackToFocus = onBackToFocus,
                onNotNow = onNotNow,
                onEmergencyPass = onEmergencyPass,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

/**
 * Upper hero content for the blocked app overlay (icon, title, subtitle, countdown pill).
 */
@Composable
private fun BlockerHeroContent(
    appName: String,
    appIconBitmap: ImageBitmap?,
    subtitle: String,
    reason: BlockReason,
    remainingSessionDurationMs: Long?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ── App Icon with Radiating Accents & Red Minus Badge ──────────────
        RadiantAppIcon(
            iconBitmap = appIconBitmap,
            appName = appName,
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ── Headline: "[AppName]\nis blocked" in elegant Serif ──────────────
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = HeadlineNameColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(appName)
                }
                append("\n")
                withStyle(
                    SpanStyle(
                        color = ForestGreenPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append("is blocked")
                }
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                lineHeight = 35.sp,
                letterSpacing = (-0.3).sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Subtitle / Reason ──────────────────────────────────────────────
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = SubtitleTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ── Session Countdown Pill ─────────────────────────────────────────
        SessionCountdownPill(
            reason = reason,
            remainingDurationMs = remainingSessionDurationMs,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Centered App Icon container with radiating focus accents and a bottom-right restriction badge.
 */
@Composable
private fun RadiantAppIcon(
    iconBitmap: ImageBitmap?,
    appName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(148.dp),
        contentAlignment = Alignment.Center,
    ) {
        // ── 6 Decorative Radiating Focus Accents ──────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            // Angles: Upper-left (-115°), Upper-right (-65°), Mid-left (-170°),
            // Mid-right (-10°), Lower-left (150°), Lower-right (30°)
            val angles = floatArrayOf(-115f, -65f, -170f, -10f, 150f, 30f)
            val dashLength = 12.dp.toPx()
            val startDist = 64.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            angles.forEach { angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble())
                val cos = Math.cos(rad).toFloat()
                val sin = Math.sin(rad).toFloat()
                val startX = center.x + cos * startDist
                val startY = center.y + sin * startDist
                val endX = center.x + cos * (startDist + dashLength)
                val endY = center.y + sin * (startDist + dashLength)

                drawLine(
                    color = RayAccentColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }

        // ── Blocked App Squircle Card Container ───────────────────────────────
        Box(modifier = Modifier.size(104.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x22000000),
                        ambientColor = Color(0x10000000),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = appName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    FourSquareAppIconFallback()
                }
            }

            // ── Red Restriction Minus Badge Overlaid at Bottom-Right ──────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(34.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = BadgeShadowColor)
                    .clip(CircleShape)
                    .background(BadgeRedColor)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 3.dp)
                        .background(Color.White, RoundedCornerShape(1.5.dp)),
                )
            }
        }
    }
}

/**
 * 2x2 grid representing the launcher app icon style matching the reference design.
 */
@Composable
private fun FourSquareAppIconFallback(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GridFallbackColor),
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GridFallbackColor),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GridFallbackColor),
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(GridFallbackColor),
            )
        }
    }
}

/**
 * Pill-shaped session countdown indicator (e.g., "18:42 left in this session").
 */
@Composable
private fun SessionCountdownPill(
    reason: BlockReason,
    remainingDurationMs: Long?,
    modifier: Modifier = Modifier,
) {
    val sessionTimeText = remember(remainingDurationMs) {
        if (remainingDurationMs != null && remainingDurationMs > 0L) {
            val totalSeconds = (remainingDurationMs / 1000L).coerceAtLeast(0L)
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } else {
            "18:42"
        }
    }

    val (pillIcon, pillText) = remember(reason, sessionTimeText) {
        when (reason) {
            BlockReason.IN_FOCUS_SESSION ->
                Icons.Outlined.Schedule to "$sessionTimeText left in this session"
            BlockReason.ACTIVE_SCHEDULE ->
                Icons.Outlined.Schedule to "Schedule active"
            BlockReason.TIME_LIMIT_EXCEEDED ->
                Icons.Outlined.HourglassEmpty to "Daily time limit reached"
            BlockReason.LAUNCH_LIMIT_EXCEEDED ->
                Icons.Outlined.HourglassEmpty to "Daily launch limit reached"
            BlockReason.SHORT_FORM_FEED ->
                Icons.Outlined.VisibilityOff to "Short-form feed blocked"
            BlockReason.STUDY_MODE_CHANNEL_NOT_ALLOWLISTED ->
                Icons.Outlined.School to "Study Mode active"
            BlockReason.ALWAYS_BLOCKED ->
                Icons.Outlined.Lock to "Always blocked"
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0x10000000),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .border(BorderStroke(1.dp, PillBorderColor), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = pillIcon,
                contentDescription = null,
                tint = ForestGreenPrimary,
                modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = pillText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = ForestGreenPrimary,
            )
        }
    }
}

/**
 * Action buttons: "Back to Focus", "Not Now", and "5-Min Emergency Pass".
 */
@Composable
private fun BlockerActionButtons(
    remainingEmergencyPasses: Int,
    onBackToFocus: () -> Unit,
    onNotNow: () -> Unit,
    onEmergencyPass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Primary Action: "Back to Focus ->" ─────────────────────────────
        Button(
            onClick = onBackToFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 5.dp,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Back to Focus",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Secondary Action: "Not Now" Outlined Pill Button ───────────────
        Button(
            onClick = onNotNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NotNowButtonBg,
                contentColor = NotNowTextColor,
            ),
            border = BorderStroke(1.5.dp, NotNowBorderColor),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
            ),
        ) {
            Text(
                text = "Not Now",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = NotNowTextColor,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Emergency Pass (Safety Exit Option) ────────────────────────────
        val passesText = if (remainingEmergencyPasses > 0) {
            "$remainingEmergencyPasses left"
        } else {
            "None left"
        }
        TextButton(
            onClick = onEmergencyPass,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = EmergencyPassColor,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "5-Min Emergency Pass · $passesText",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = EmergencyPassColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockerOverlayContentPreview() {
    FocusFlowTheme {
        BlockerOverlayContent(
            packageName = "com.android.launcher",
            decision = BlockingDecision.Block(
                reason = BlockReason.IN_FOCUS_SESSION,
                message = "This app is blocked during your focus session.",
            ),
            remainingSessionDurationMs = (18 * 60 + 42) * 1000L,
            remainingEmergencyPasses = 2,
            onBackToFocus = {},
            onNotNow = {},
            onEmergencyPass = {},
        )
    }
}
