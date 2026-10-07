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
    }
}
