package com.focusflow.core.domain.repository

import com.focusflow.core.domain.model.SubjectTag
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing subject tags.
 */
interface SubjectTagRepository {

    /**
     * Emits all available subject tags.
     */
    fun getAllTags(): Flow<List<SubjectTag>>

    /**
     * Adds or updates a subject tag.
     */
    suspend fun saveTag(tag: SubjectTag)

    /**
     * Deletes a subject tag by id.
     */
    suspend fun deleteTag(tagId: String)
}
