package com.focusflow.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focusflow.core.data.local.dao.BlockingDao
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.dao.SocialDao
import com.focusflow.core.data.local.dao.StudyModeDao
import com.focusflow.core.data.local.dao.SubjectTagDao
import com.focusflow.core.data.local.dao.UsageDao
import com.focusflow.core.data.local.entity.AppLimitEntity
import com.focusflow.core.data.local.entity.AppUsageEntity
import com.focusflow.core.data.local.entity.BlockRuleEntity
import com.focusflow.core.data.local.entity.BlockScheduleEntity
import com.focusflow.core.data.local.entity.DailyUsageEntity
import com.focusflow.core.data.local.entity.EmergencyPassEntity
import com.focusflow.core.data.local.entity.FocusSessionEntity
import com.focusflow.core.data.local.entity.FriendEntity
import com.focusflow.core.data.local.entity.LeaderboardEntryEntity
import com.focusflow.core.data.local.entity.RoomEntity
import com.focusflow.core.data.local.entity.StudyChannelEntity
import com.focusflow.core.data.local.entity.SubjectTagEntity
import com.focusflow.core.data.local.entity.UserEssentialAppEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        SubjectTagEntity::class,
        DailyUsageEntity::class,
        AppUsageEntity::class,
        UserEssentialAppEntity::class,
        BlockRuleEntity::class,
        AppLimitEntity::class,
        BlockScheduleEntity::class,
        EmergencyPassEntity::class,
        RoomEntity::class,
        FriendEntity::class,
        LeaderboardEntryEntity::class,
        StudyChannelEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun subjectTagDao(): SubjectTagDao
    abstract fun usageDao(): UsageDao
    abstract fun blockingDao(): BlockingDao
    abstract fun socialDao(): SocialDao
    abstract fun studyModeDao(): StudyModeDao
}
