package com.focusflow.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focusflow.core.data.local.FocusFlowDatabase
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.dao.SubjectTagDao
import com.focusflow.core.data.local.entity.SubjectTagEntity
import com.focusflow.core.data.repository.OfflineFocusSessionRepository
import com.focusflow.core.data.repository.OfflineSubjectTagRepository
import com.focusflow.core.domain.repository.FocusSessionRepository
import com.focusflow.core.domain.repository.SubjectTagRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFocusSessionRepository(
        impl: OfflineFocusSessionRepository,
    ): FocusSessionRepository

    @Binds
    @Singleton
    abstract fun bindSubjectTagRepository(
        impl: OfflineSubjectTagRepository,
    ): SubjectTagRepository

    @Binds
    @Singleton
    abstract fun bindUsageStatsRepository(
        impl: com.focusflow.core.data.repository.OfflineUsageStatsRepository,
    ): com.focusflow.core.domain.usage.repository.UsageStatsRepository

    @Binds
    @Singleton
    abstract fun bindAppListRepository(
        impl: com.focusflow.core.data.repository.AndroidAppListRepository,
    ): com.focusflow.core.domain.usage.repository.AppListRepository

    @Binds
    @Singleton
    abstract fun bindBlockRuleRepository(
        impl: com.focusflow.core.data.repository.OfflineBlockRuleRepository,
    ): com.focusflow.core.domain.blocking.repository.BlockRuleRepository

    @Binds
    @Singleton
    abstract fun bindAppLimitRepository(
        impl: com.focusflow.core.data.repository.OfflineAppLimitRepository,
    ): com.focusflow.core.domain.blocking.repository.AppLimitRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyPassRepository(
        impl: com.focusflow.core.data.repository.OfflineEmergencyPassRepository,
    ): com.focusflow.core.domain.blocking.repository.EmergencyPassRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: com.focusflow.core.data.auth.FirebaseAuthRepository,
    ): com.focusflow.core.domain.auth.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: com.focusflow.core.data.sync.FirestoreSyncRepository,
    ): com.focusflow.core.domain.sync.repository.SyncRepository

    @Binds
    @Singleton
    abstract fun bindPresenceRepository(
        impl: com.focusflow.core.data.social.FirebasePresenceRepository,
    ): com.focusflow.core.domain.social.repository.PresenceRepository

    @Binds
    @Singleton
    abstract fun bindFocusRoomRepository(
        impl: com.focusflow.core.data.social.FirestoreFocusRoomRepository,
    ): com.focusflow.core.domain.social.repository.FocusRoomRepository

    @Binds
    @Singleton
    abstract fun bindFriendsRepository(
        impl: com.focusflow.core.data.social.FirestoreFriendsRepository,
    ): com.focusflow.core.domain.social.repository.FriendsRepository

    @Binds
    @Singleton
    abstract fun bindLeaderboardRepository(
        impl: com.focusflow.core.data.social.FirestoreLeaderboardRepository,
    ): com.focusflow.core.domain.social.repository.LeaderboardRepository

    @Binds
    @Singleton
    abstract fun bindStudyModeRepository(
        impl: com.focusflow.core.data.repository.OfflineStudyModeRepository,
    ): com.focusflow.core.domain.blocking.repository.StudyModeRepository

    @Binds
    @Singleton
    abstract fun bindAdvancedBlockingRepository(
        impl: com.focusflow.core.data.repository.OfflineAdvancedBlockingRepository,
    ): com.focusflow.core.domain.blocking.repository.AdvancedBlockingRepository

    companion object {

        @Provides
        @Singleton
        fun provideFocusFlowDatabase(
            @ApplicationContext context: Context,
            tagDaoProvider: Provider<SubjectTagDao>,
        ): FocusFlowDatabase {
            return Room.databaseBuilder(
                context,
                FocusFlowDatabase::class.java,
                "focusflow.db",
            )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Prepopulate with default starter tags per PROJECT_BRIEF.md
                    CoroutineScope(Dispatchers.IO).launch {
                        tagDaoProvider.get().insertDefaultTags(
                            listOf(
                                SubjectTagEntity(
                                    id = "tag_deep_work",
                                    name = "Deep Work",
                                    colorHex = "#3D6BCC",
                                    iconName = "work",
                                    isDefault = true,
                                ),
                                SubjectTagEntity(
                                    id = "tag_study",
                                    name = "Study",
                                    colorHex = "#2E8B7A",
                                    iconName = "school",
                                    isDefault = true,
                                ),
                                SubjectTagEntity(
                                    id = "tag_coding",
                                    name = "Coding",
                                    colorHex = "#7B4DBC",
                                    iconName = "code",
                                    isDefault = true,
                                ),
                                SubjectTagEntity(
                                    id = "tag_reading",
                                    name = "Reading",
                                    colorHex = "#E8A838",
                                    iconName = "book",
                                    isDefault = true,
                                ),
                            )
                        )
                    }
                }
            })
            .build()
        }

        @Provides
        fun provideFocusSessionDao(database: FocusFlowDatabase): FocusSessionDao =
            database.focusSessionDao()

        @Provides
        fun provideSubjectTagDao(database: FocusFlowDatabase): SubjectTagDao =
            database.subjectTagDao()

        @Provides
        fun provideUsageDao(
            database: FocusFlowDatabase,
        ): com.focusflow.core.data.local.dao.UsageDao =
            database.usageDao()

        @Provides
        fun provideBlockingDao(
            database: FocusFlowDatabase,
        ): com.focusflow.core.data.local.dao.BlockingDao =
            database.blockingDao()

        @Provides
        fun provideSocialDao(
            database: FocusFlowDatabase,
        ): com.focusflow.core.data.local.dao.SocialDao =
            database.socialDao()

        @Provides
        fun provideStudyModeDao(
            database: FocusFlowDatabase,
        ): com.focusflow.core.data.local.dao.StudyModeDao =
            database.studyModeDao()
    }
}
