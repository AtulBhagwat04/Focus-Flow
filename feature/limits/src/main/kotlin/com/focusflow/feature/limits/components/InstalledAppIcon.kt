package com.focusflow.feature.limits.components

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ICON_CACHE_MAX_ENTRIES = 250
private const val DEFAULT_ICON_PIXEL_SIZE = 96
private const val MIN_ICON_PIXEL_SIZE = 48
private const val MAX_ICON_PIXEL_SIZE = 256

internal object AppIconCache {
    private val memoryCache = object : LruCache<String, ImageBitmap>(ICON_CACHE_MAX_ENTRIES) {}

    fun get(packageName: String): ImageBitmap? = memoryCache.get(packageName)

    fun put(packageName: String, bitmap: ImageBitmap) {
        memoryCache.put(packageName, bitmap)
    }
}

internal fun Drawable.toImageBitmap(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && bitmap != null) {
            return bitmap.asImageBitmap()
        }
        val width = if (intrinsicWidth > 0) intrinsicWidth else DEFAULT_ICON_PIXEL_SIZE
        val height = if (intrinsicHeight > 0) intrinsicHeight else DEFAULT_ICON_PIXEL_SIZE
        val safeWidth = width.coerceIn(MIN_ICON_PIXEL_SIZE, MAX_ICON_PIXEL_SIZE)
        val safeHeight = height.coerceIn(MIN_ICON_PIXEL_SIZE, MAX_ICON_PIXEL_SIZE)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    } catch (_: OutOfMemoryError) {
        null
    }
}

internal suspend fun loadAppIconBitmap(context: Context, packageName: String): ImageBitmap? {
    val cached = AppIconCache.get(packageName)
    if (cached != null) return cached

    return withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawable.toImageBitmap()
            if (bitmap != null) {
                AppIconCache.put(packageName, bitmap)
            }
            bitmap
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}

private val MONOGRAM_PALETTE = listOf(
    Color(0xFFC22323), // Dark Crimson Red (AbhiBus style)
    Color(0xFFE50914), // Bright Red (Airtel style)
    Color(0xFFE6A467), // Warm Amber (Amazon style)
    Color(0xFF4C3B84), // Deep Purple (Apna style)
    Color(0xFF1DB954), // Forest Vibrant Green (App Market style)
    Color(0xFF4285F4), // Royal Blue (Assistant style)
    Color(0xFF0F9D58), // Emerald
    Color(0xFFDB4437), // Coral
    Color(0xFF673AB7), // Violet
    Color(0xFF00897B), // Teal
    Color(0xFFE65100), // Tangerine
    Color(0xFF3F51B5), // Indigo
)

private fun getMonogram(appName: String): String {
    val trimmed = appName.trim()
    if (trimmed.isEmpty()) return "A"
    val words = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (words.size >= 2) {
        val first = words[0].first()
        val second = words[1].first()
        "$first$second"
    } else {
        trimmed.take(2)
    }
}

private fun getMonogramColor(key: String): Color {
    val index = kotlin.math.abs(key.hashCode()) % MONOGRAM_PALETTE.size
    return MONOGRAM_PALETTE[index]
}

/**
 * Renders an installed app's system icon with in-memory caching and asynchronous loading.
 * Shows a brand-colored monogram badge as a smooth, instant fallback while loading or if unavailable.
 */
@Composable
fun InstalledAppIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    val context = LocalContext.current
    val initialCached = AppIconCache.get(packageName)

    val iconBitmapState = produceState(initialValue = initialCached, key1 = packageName) {
        if (value == null) {
            value = loadAppIconBitmap(context, packageName)
        }
    }

    val iconBitmap = iconBitmapState.value

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = "$appName icon",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(14.dp)),
            )
        } else {
            val monogram = getMonogram(appName)
            val bgColor = getMonogramColor(packageName.ifEmpty { appName })
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = monogram,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = if (monogram.length > 1) 16.sp else 18.sp,
                )
            }
        }
    }
}
