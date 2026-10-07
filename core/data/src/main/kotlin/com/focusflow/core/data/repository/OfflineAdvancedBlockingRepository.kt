package com.focusflow.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig
import com.focusflow.core.domain.blocking.model.RemoteDetectionSelectors
import com.focusflow.core.domain.blocking.repository.AdvancedBlockingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.advancedBlockingDataStore by preferencesDataStore(name = "advanced_blocking_prefs")

@Singleton
class OfflineAdvancedBlockingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AdvancedBlockingRepository {

    private val KEY_SHORTS_BLOCKED = booleanPreferencesKey("shorts_blocking_enabled")
    private val KEY_REELS_BLOCKED = booleanPreferencesKey("reels_blocking_enabled")
    private val KEY_FOCUS_ONLY = booleanPreferencesKey("feed_focus_only")

    override fun getConfig(): Flow<AdvancedBlockingConfig> {
        return context.advancedBlockingDataStore.data.map { prefs ->
            AdvancedBlockingConfig(
                isShortsBlockingEnabled = prefs[KEY_SHORTS_BLOCKED] ?: false,
                isReelsBlockingEnabled = prefs[KEY_REELS_BLOCKED] ?: false,
                blockDuringFocusOnly = prefs[KEY_FOCUS_ONLY] ?: true,
                isKillSwitchActive = false,
            )
        }
    }

    override suspend fun setShortsBlockingEnabled(enabled: Boolean): Result<Unit> = runCatching {
        context.advancedBlockingDataStore.edit { prefs ->
            prefs[KEY_SHORTS_BLOCKED] = enabled
        }
    }

    override suspend fun setReelsBlockingEnabled(enabled: Boolean): Result<Unit> = runCatching {
        context.advancedBlockingDataStore.edit { prefs ->
            prefs[KEY_REELS_BLOCKED] = enabled
        }
    }

    override suspend fun setBlockDuringFocusOnly(focusOnly: Boolean): Result<Unit> = runCatching {
        context.advancedBlockingDataStore.edit { prefs ->
            prefs[KEY_FOCUS_ONLY] = focusOnly
        }
    }

    override fun getRemoteSelectors(): Flow<RemoteDetectionSelectors> {
        // Fallback robust selectors; will dynamically update if Remote Config provides overrides
        return flowOf(
            RemoteDetectionSelectors(
                youtubeShortsViewIds = listOf(
                    "shorts_container",
                    "reel_recycler_view",
                    "shorts_pivot_item",
                    "reel_player_page_container",
                ),
                instagramReelsViewIds = listOf(
                    "clips_video_container",
                    "reel_viewer_root",
                    "clips_viewer_view_pager",
                ),
                killSwitchEnabled = false,
            )
        )
    }
}
