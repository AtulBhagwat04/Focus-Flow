package com.focusflow.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.focusflow.core.data.local.dao.StudyModeDao
import com.focusflow.core.data.local.entity.StudyChannelEntity
import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig
import com.focusflow.core.domain.blocking.repository.StudyModeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.studyModeDataStore by preferencesDataStore(name = "study_mode_prefs")

@Singleton
class OfflineStudyModeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val studyModeDao: StudyModeDao,
) : StudyModeRepository {

    private val KEY_STUDY_MODE_ENABLED = booleanPreferencesKey("study_mode_enabled")
    private val KEY_FAIL_CLOSED = booleanPreferencesKey("fail_closed")



    override fun getStudyModeConfig(): Flow<StudyModeConfig> {
        val prefsFlow = context.studyModeDataStore.data.map { prefs ->
            val enabled = prefs[KEY_STUDY_MODE_ENABLED] ?: false
            val failClosed = prefs[KEY_FAIL_CLOSED] ?: false
            Pair(enabled, failClosed)
        }

        return combine(prefsFlow, studyModeDao.getAllowlistedChannels()) { (enabled, failClosed), channels ->
            StudyModeConfig(
                isEnabled = enabled,
                allowlistedChannelIds = channels.map { it.channelId }.toSet(),
                allowlistedChannelNames = channels.map { it.channelTitle }.toSet(),
                failClosed = failClosed,
                isKillSwitchActive = false,
            )
        }
    }

    override suspend fun setStudyModeEnabled(enabled: Boolean): Result<Unit> = runCatching {
        context.studyModeDataStore.edit { prefs ->
            prefs[KEY_STUDY_MODE_ENABLED] = enabled
        }
    }

    override suspend fun setFailClosed(failClosed: Boolean): Result<Unit> = runCatching {
        context.studyModeDataStore.edit { prefs ->
            prefs[KEY_FAIL_CLOSED] = failClosed
        }
    }

    override fun getAllowlistedChannels(): Flow<List<StudyChannel>> {
        return studyModeDao.getAllChannels().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addChannel(channel: StudyChannel): Result<Unit> = runCatching {
        studyModeDao.insertChannel(
            StudyChannelEntity(
                channelId = channel.channelId,
                channelTitle = channel.channelTitle,
                thumbnailUrl = channel.thumbnailUrl,
                category = channel.category,
                isCuratedDefault = channel.isCuratedDefault,
                isAllowlisted = channel.isAllowlisted,
            )
        )
    }

    override suspend fun removeChannel(channelId: String): Result<Unit> = runCatching {
        studyModeDao.deleteChannel(channelId)
    }

    override suspend fun toggleChannelAllowlist(channelId: String, isAllowlisted: Boolean): Result<Unit> = runCatching {
        studyModeDao.setAllowlisted(channelId, isAllowlisted)
    }



    private fun StudyChannelEntity.toDomain() = StudyChannel(
        channelId = channelId,
        channelTitle = channelTitle,
        thumbnailUrl = thumbnailUrl,
        category = category,
        isCuratedDefault = isCuratedDefault,
        isAllowlisted = isAllowlisted,
    )
}
