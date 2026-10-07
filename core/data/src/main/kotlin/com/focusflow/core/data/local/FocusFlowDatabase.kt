package com.focusflow.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focusflow.core.data.local.dao.FocusSessionDao
import com.focusflow.core.data.local.dao.SubjectTagDao
import com.focusflow.core.data.local.entity.FocusSessionEntity
import com.focusflow.core.data.local.entity.SubjectTagEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        SubjectTagEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun subjectTagDao(): SubjectTagDao
}
