package com.focusflow.core.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.focusflow.core.domain.safelist.SafeListPolicy
import com.focusflow.core.domain.usage.model.AppUsageSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Raw usage metrics extracted from Android's [UsageStatsManager].
 */
data class RawDailyUsage(
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val appSummaries: List<AppUsageSummary>,
)

/**
 * Reads device usage events and statistics directly from [UsageStatsManager].
 */
@Singleton
class AndroidUsageStatsReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager: PackageManager = context.packageManager

    /**
     * Reads usage metrics for a given epoch day.
     */
    fun readUsageForDay(epochDay: Long, userEssentials: Set<String> = emptySet()): RawDailyUsage {
        if (usageStatsManager == null) {
            return RawDailyUsage(totalScreenTimeMs = 0L, unlockCount = 0, appSummaries = emptyList())
        }

        val zoneId = ZoneId.systemDefault()
        val date = LocalDate.ofEpochDay(epochDay)
        val startOfDayMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDayMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val nowMs = System.currentTimeMillis()
        val actualEndMs = minOf(endOfDayMs, nowMs)

        if (actualEndMs <= startOfDayMs) {
            return RawDailyUsage(totalScreenTimeMs = 0L, unlockCount = 0, appSummaries = emptyList())
        }

        // Query usage stats
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDayMs,
            actualEndMs,
        ) ?: emptyList()

        // Query events for unlocks and launch counts
        var unlockCount = 0
        val launchCounts = mutableMapOf<String, Int>()

        try {
            val events = usageStatsManager.queryEvents(startOfDayMs, actualEndMs)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.KEYGUARD_DISMISSED,
                    UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        unlockCount++
                    }
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        val pkg = event.packageName
                        if (!pkg.isNullOrBlank()) {
                            launchCounts[pkg] = (launchCounts[pkg] ?: 0) + 1
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Degrades gracefully if event query fails
        }

        val summaries = statsList
            .filter { it.totalTimeInForeground > 0L }
            .map { stat ->
                val pkg = stat.packageName
                val label = try {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg
                }
                val isSafe = SafeListPolicy.isSafeListed(
                    packageName = pkg,
                    userEssentials = userEssentials,
                )
                AppUsageSummary(
                    packageName = pkg,
                    appName = label,
                    totalTimeForegroundMs = stat.totalTimeInForeground,
                    launchCount = launchCounts[pkg] ?: 1,
                    lastTimeUsedEpochMs = stat.lastTimeUsed,
                    isSafeListed = isSafe,
                )
            }
            .sortedByDescending { it.totalTimeForegroundMs }

        val totalScreenTimeMs = summaries.sumOf { it.totalTimeForegroundMs }

        return RawDailyUsage(
            totalScreenTimeMs = totalScreenTimeMs,
            unlockCount = maxOf(unlockCount / 2, 0), // Screen interactive + keyguard dismiss normalization
            appSummaries = summaries,
        )
    }
}
