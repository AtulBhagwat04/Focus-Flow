package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.dao.UsageDao
import com.focusflow.core.data.local.entity.AppUsageEntity
import com.focusflow.core.data.local.entity.DailyUsageEntity
import com.focusflow.core.data.usage.AndroidUsageStatsReader
import com.focusflow.core.domain.usage.model.AppUsageSummary
import com.focusflow.core.domain.usage.model.DailyUsageStats
import com.focusflow.core.domain.usage.repository.AppListRepository
import com.focusflow.core.domain.usage.repository.UsageStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first repository coordinating Room persistence with Android's usage events reader.
 */
@Singleton
class OfflineUsageStatsRepository @Inject constructor(
    private val usageDao: UsageDao,
    private val focusSessionDao: FocusSessionDao,
    private val usageReader: AndroidUsageStatsReader,
    private val appListRepository: AppListRepository,
) : UsageStatsRepository {

    override fun observeDailyUsage(epochDay: Long): Flow<DailyUsageStats> {
        val zoneId = ZoneId.systemDefault()
        val date = LocalDate.ofEpochDay(epochDay)
        val startOfDayMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDayMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val dailyFlow = usageDao.observeDailyUsage(epochDay)
        val appsFlow = usageDao.observeAppUsagesForDay(epochDay)
        val focusFlow = focusSessionDao.observeFocusDurationBetween(startOfDayMs, endOfDayMs)

        return combine(dailyFlow, appsFlow, focusFlow) { dailyEntity, appEntities, focusDurationMs ->
            val topApps = appEntities.map { entity ->
                AppUsageSummary(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    totalTimeForegroundMs = entity.totalTimeForegroundMs,
                    launchCount = entity.launchCount,
                    lastTimeUsedEpochMs = entity.lastTimeUsedEpochMs,
                    isSafeListed = entity.isSafeListed,
                )
            }

            DailyUsageStats(
                epochDay = epochDay,
                totalScreenTimeMs = dailyEntity?.totalScreenTimeMs ?: 0L,
                totalFocusTimeMs = focusDurationMs,
                unlockCount = dailyEntity?.unlockCount ?: 0,
                topApps = topApps,
            )
        }
    }

    override fun observeDateRangeUsage(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyUsageStats>> {
        return usageDao.observeDateRangeUsage(startEpochDay, endEpochDay).map { list ->
            list.map { entity ->
                DailyUsageStats(
                    epochDay = entity.epochDay,
                    totalScreenTimeMs = entity.totalScreenTimeMs,
                    totalFocusTimeMs = entity.totalFocusTimeMs,
                    unlockCount = entity.unlockCount,
                    topApps = emptyList(),
                )
            }
        }
    }

    override suspend fun refreshUsageForDay(epochDay: Long) {
        val essentials = appListRepository.getUserEssentials().first()
        val raw = usageReader.readUsageForDay(epochDay, essentials)

        val dailyEntity = DailyUsageEntity(
            epochDay = epochDay,
            totalScreenTimeMs = raw.totalScreenTimeMs,
            totalFocusTimeMs = 0L,
            unlockCount = raw.unlockCount,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        usageDao.insertDailyUsage(dailyEntity)

        val appEntities = raw.appSummaries.map { summary ->
            AppUsageEntity(
                id = "${epochDay}_${summary.packageName}",
                epochDay = epochDay,
                packageName = summary.packageName,
                appName = summary.appName,
                totalTimeForegroundMs = summary.totalTimeForegroundMs,
                launchCount = summary.launchCount,
                lastTimeUsedEpochMs = summary.lastTimeUsedEpochMs,
                isSafeListed = summary.isSafeListed,
            )
        }
        usageDao.insertAppUsages(appEntities)
    }
}
