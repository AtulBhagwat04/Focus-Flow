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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [AppListRepository].
 * Resolves launchable packages safely on [Dispatchers.IO] and syncs user-essential overrides with Room.
 */
@Singleton
class AndroidAppListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageDao: UsageDao,
) : AppListRepository {

    private val packageManager: PackageManager = context.packageManager
    private val prefs = context.getSharedPreferences("focusflow_installed_apps_cache", Context.MODE_PRIVATE)

    @Volatile
    private var cachedRawApps: List<Pair<String, String>>? = null

    init {
        // Fast synchronous load of cached apps on initialization
        val fromDisk = loadCachedPackages()
        if (fromDisk.isNotEmpty()) {
            cachedRawApps = fromDisk
        }
        // Warm up / refresh in background immediately on repo creation
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val list = queryInstalledPackages()
            if (list.isNotEmpty() && list != cachedRawApps) {
                cachedRawApps = list
                saveCachedPackages(list)
            }
        }
    }

    private fun loadCachedPackages(): List<Pair<String, String>> {
        return try {
            val set = prefs.getStringSet("cached_apps", null) ?: return emptyList()
            set.mapNotNull { entry ->
                val separatorIndex = entry.indexOf('|')
                if (separatorIndex > 0) {
                    entry.substring(0, separatorIndex) to entry.substring(separatorIndex + 1)
                } else null
            }.sortedBy { it.second.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCachedPackages(apps: List<Pair<String, String>>) {
        try {
            val set = apps.map { "${it.first}|${it.second}" }.toSet()
            prefs.edit().putStringSet("cached_apps", set).apply()
        } catch (_: Exception) {
            // Ignore storage issues defensively
        }
    }

    /**
     * Resolves all launchable apps with launcher activities safely.
     * Uses defensive exception handling to prevent crashes from corrupt packages.
     */
    private fun queryInstalledPackages(): List<Pair<String, String>> {
        return try {
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

            resolveInfos.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val label = try {
                    resolveInfo.loadLabel(packageManager).toString()
                } catch (_: Exception) {
                    activityInfo.packageName
                }
                activityInfo.packageName to label
            }.distinctBy { it.first }.sortedBy { it.second.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun rawInstalledAppsFlow(): Flow<List<Pair<String, String>>> = flow {
        // 1. Instantly emit cached list (0ms latency!)
        var current = cachedRawApps
        if (current == null) {
            val fromDisk = loadCachedPackages()
            if (fromDisk.isNotEmpty()) {
                cachedRawApps = fromDisk
                current = fromDisk
            }
        }
        if (current != null && current.isNotEmpty()) {
            emit(current)
        }

        // 2. Query PackageManager in background and update if list changed
        val freshlyQueried = queryInstalledPackages()
        if (freshlyQueried.isNotEmpty()) {
            if (current != freshlyQueried) {
                cachedRawApps = freshlyQueried
                saveCachedPackages(freshlyQueried)
                emit(freshlyQueried)
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getInstalledApps(): Flow<List<InstalledApp>> =
        combine(rawInstalledAppsFlow(), getUserEssentials()) { rawApps, essentials ->
            rawApps
                .distinctBy { it.first }
                .map { (packageName, label) ->
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
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
        }.flowOn(Dispatchers.IO)

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
