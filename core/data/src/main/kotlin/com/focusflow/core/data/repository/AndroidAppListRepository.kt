package com.focusflow.core.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.focusflow.core.data.local.dao.UsageDao
import com.focusflow.core.data.local.entity.UserEssentialAppEntity
import com.focusflow.core.domain.safelist.SafeListPolicy
import com.focusflow.core.domain.usage.model.AppCategory
import com.focusflow.core.domain.usage.model.InstalledApp
import com.focusflow.core.domain.usage.repository.AppListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [AppListRepository].
 * Resolves launchable packages and syncs user-essential overrides with Room.
 */
@Singleton
class AndroidAppListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageDao: UsageDao,
) : AppListRepository {

    private val packageManager: PackageManager = context.packageManager

    override fun getInstalledApps(): Flow<List<InstalledApp>> {
        val rawAppsFlow = flow {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    mainIntent,
                    PackageManager.ResolveInfoFlags.of(0L),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(mainIntent, 0)
            }

            val appList = resolveInfos.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                packageName to label
            }
            emit(appList)
        }.flowOn(Dispatchers.IO)

        return combine(rawAppsFlow, getUserEssentials()) { rawApps, essentials ->
            rawApps.map { (packageName, label) ->
                val isEssential = essentials.contains(packageName)
                val isSafe = SafeListPolicy.isSafeListed(
                    packageName = packageName,
                    userEssentials = essentials,
                )
                InstalledApp(
                    packageName = packageName,
                    label = label,
                    isSafeListed = isSafe,
                    isEssential = isEssential,
                    category = AppCategory.OTHER,
                )
            }.sortedBy { it.label.lowercase() }
        }
    }

    override fun getUserEssentials(): Flow<Set<String>> {
        return usageDao.observeUserEssentials().map { list ->
            list.map { it.packageName }.toSet()
        }
    }

    override suspend fun setUserEssential(packageName: String, isEssential: Boolean) {
        if (isEssential) {
            usageDao.insertUserEssential(
                UserEssentialAppEntity(
                    packageName = packageName,
                    addedAtEpochMs = System.currentTimeMillis(),
                )
            )
        } else {
            usageDao.deleteUserEssential(packageName)
        }
    }
}
