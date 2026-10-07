package com.focusflow.core.domain.usage.repository

import com.focusflow.core.domain.usage.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * Access to installed launchable applications and user-designated essential apps.
 */
interface AppListRepository {
    /**
     * Observes the list of installed launchable apps on the device.
     */
    fun getInstalledApps(): Flow<List<InstalledApp>>

    /**
     * Observes the package names marked by the user as essential (Safe List protected).
     */
    fun getUserEssentials(): Flow<Set<String>>

    /**
     * Marks or unmarks a package name as user-essential.
     */
    suspend fun setUserEssential(packageName: String, isEssential: Boolean)
}
