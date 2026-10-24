package com.focusflow.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Access and modification of user application preferences and goals.
 *
 * Persisted locally so that settings such as the weekly focus goal are
 * preserved across app launches and shared reactively across features.
 */
interface UserPreferencesRepository {

    /**
     * Observes the target weekly focus goal in hours (default 16).
     */
    fun getWeeklyGoalHours(): Flow<Int>

    /**
     * Updates the target weekly focus goal in hours.
     */
    suspend fun setWeeklyGoalHours(hours: Int)

    /**
     * Observes whether focus session reminders are enabled.
     */
    fun getRemindersEnabled(): Flow<Boolean>

    /**
     * Updates the focus session reminder setting.
     */
    suspend fun setRemindersEnabled(enabled: Boolean)

    /**
     * Observes the active appearance theme name.
     */
    fun getAppearanceTheme(): Flow<String>

    /**
     * Updates the active appearance theme name.
     */
    suspend fun setAppearanceTheme(theme: String)
}
