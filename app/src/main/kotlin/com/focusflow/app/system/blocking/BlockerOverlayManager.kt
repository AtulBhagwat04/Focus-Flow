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
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.domain.blocking.model.BlockReason
import com.focusflow.core.domain.blocking.model.BlockingDecision
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockerOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context,
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
                    BlockerOverlayContent(
                        packageName = packageName,
                        decision = decision,
                        onBackToFocus = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
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

@Composable
internal fun BlockerOverlayContent(
    packageName: String,
    decision: BlockingDecision.Block,
    onBackToFocus: () -> Unit,
    onNotNow: () -> Unit,
    onEmergencyPass: () -> Unit,
    modifier: Modifier = Modifier,
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
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
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
            .background(Color(0xFFF7F8F4)),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── App Icon with Radiating Accents & Red Minus Badge ──────────────
            RadiantAppIcon(
                iconBitmap = appIconBitmap,
                appName = appName,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Headline: "[App Name] is blocked" ──────────────────────────────
            Text(
                text = "$appName is blocked",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                ),
                color = FocusDarkText,
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
                color = FocusMutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Motivational Card: "Stay Focused" with Leaf Icon ───────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF5ED)),
                border = BorderStroke(1.dp, Color(0xFFE2ECE0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCE7D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            tint = Color(0xFF1E4D38),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stay Focused",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            ),
                            color = Color(0xFF183D2B),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You're doing great! Keep going towards your goals.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Color(0xFF4D6356),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Primary Action: "Back to Focus ->" ─────────────────────────────
            Button(
                onClick = onBackToFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E4D38),
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Back to Focus",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            // ── Secondary Action: "Not Now" ────────────────────────────────────
            TextButton(
                onClick = onNotNow,
                modifier = Modifier.height(44.dp),
            ) {
                Text(
                    text = "Not Now",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color(0xFF5A7065),
                )
            }

            // ── Emergency Pass (Safety Exit Option) ────────────────────────────
            TextButton(
                onClick = onEmergencyPass,
                modifier = Modifier.height(32.dp),
            ) {
                Text(
                    text = "5-Min Emergency Pass",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
                    color = Color(0xFF7A8D84),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
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
    val dashColor = Color(0xFF8FA89B)
    Box(
        modifier = modifier.size(136.dp),
        contentAlignment = Alignment.Center,
    ) {
        // ── 6 Decorative Radiating Focus Accents ──────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            // Angles symmetrically placed matching the reference design:
            // Upper-left, Top, Upper-right, Lower-right, Lower-left
            val angles = floatArrayOf(-150f, -120f, -60f, -30f, 30f, 150f)
            val dashLength = 10.dp.toPx()
            val startDist = 50.dp.toPx()
            val strokeWidth = 2.5.dp.toPx()

            angles.forEach { angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble())
                val cos = Math.cos(rad).toFloat()
                val sin = Math.sin(rad).toFloat()
                val startX = center.x + cos * startDist
                val startY = center.y + sin * startDist
                val endX = center.x + cos * (startDist + dashLength)
                val endY = center.y + sin * (startDist + dashLength)

                drawLine(
                    color = dashColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }

        // ── Blocked App Icon Squircle Container ───────────────────────────────
        Box(
            modifier = Modifier.size(80.dp),
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = appName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(22.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(FocusForestMintBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = appName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusForestGreenDeep,
                    )
                }
            }

            // ── Red Restriction Minus Badge Overlaid at Bottom-Right ──────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 5.dp, y = 5.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE54343))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 11.dp, height = 2.5.dp)
                        .background(Color.White, RoundedCornerShape(1.dp)),
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
            packageName = "com.instagram.android",
            decision = BlockingDecision.Block(
                reason = BlockReason.IN_FOCUS_SESSION,
                message = "This app is blocked during your focus session.",
            ),
            onBackToFocus = {},
            onNotNow = {},
            onEmergencyPass = {},
        )
    }
}
