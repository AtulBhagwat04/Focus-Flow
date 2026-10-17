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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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

/**
 * Renders an installed app's system icon with in-memory caching and asynchronous loading.
 * Shows an initial letter badge as a smooth fallback while loading or if unavailable.
 */
@Composable
fun InstalledAppIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
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
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = "$appName icon",
                modifier = Modifier.size(size),
            )
        } else {
            val initial = appName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
            Box(
                modifier = Modifier
                    .size(size)
                    .background(FocusForestMintBadge),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreenDeep,
                    fontSize = 17.sp,
                )
            }
        }
    }
}
