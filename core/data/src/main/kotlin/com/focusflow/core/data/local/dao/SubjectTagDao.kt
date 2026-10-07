package com.focusflow.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusflow.core.data.local.entity.SubjectTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectTagDao {

    @Query("SELECT * FROM subject_tags ORDER BY isDefault DESC, name ASC")
    fun getAllTags(): Flow<List<SubjectTagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: SubjectTagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultTags(tags: List<SubjectTagEntity>)

    @Query("DELETE FROM subject_tags WHERE id = :id")
    suspend fun deleteTag(id: String)
}
