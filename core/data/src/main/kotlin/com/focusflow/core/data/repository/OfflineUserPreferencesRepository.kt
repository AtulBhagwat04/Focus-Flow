package com.focusflow.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focusflow.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

private const val DEFAULT_WEEKLY_GOAL_HOURS = 16
private const val DEFAULT_REMINDERS_ENABLED = true
private const val DEFAULT_APPEARANCE_THEME = "Light"

@Singleton
class OfflineUserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private val KEY_WEEKLY_GOAL_HOURS = intPreferencesKey("weekly_goal_hours")
    private val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
    private val KEY_APPEARANCE_THEME = stringPreferencesKey("appearance_theme")

    override fun getWeeklyGoalHours(): Flow<Int> =
        context.userPreferencesDataStore.data.map { prefs ->
            prefs[KEY_WEEKLY_GOAL_HOURS] ?: DEFAULT_WEEKLY_GOAL_HOURS
        }

    override suspend fun setWeeklyGoalHours(hours: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_WEEKLY_GOAL_HOURS] = hours
        }
    }

    override fun getRemindersEnabled(): Flow<Boolean> =
        context.userPreferencesDataStore.data.map { prefs ->
            prefs[KEY_REMINDERS_ENABLED] ?: DEFAULT_REMINDERS_ENABLED
        }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_REMINDERS_ENABLED] = enabled
        }
    }

    override fun getAppearanceTheme(): Flow<String> =
        context.userPreferencesDataStore.data.map { prefs ->
            prefs[KEY_APPEARANCE_THEME] ?: DEFAULT_APPEARANCE_THEME
        }

    override suspend fun setAppearanceTheme(theme: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_APPEARANCE_THEME] = theme
        }
    }
}
