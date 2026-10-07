package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.SubjectTagDao
import com.focusflow.core.data.local.entity.asDomainModel
import com.focusflow.core.data.local.entity.asEntity
import com.focusflow.core.domain.model.SubjectTag
import com.focusflow.core.domain.repository.SubjectTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSubjectTagRepository @Inject constructor(
    private val subjectTagDao: SubjectTagDao,
) : SubjectTagRepository {

    override fun getAllTags(): Flow<List<SubjectTag>> =
        subjectTagDao.getAllTags().map { list ->
            list.map { it.asDomainModel() }
        }

    override suspend fun saveTag(tag: SubjectTag) {
        subjectTagDao.upsertTag(tag.asEntity())
    }

    override suspend fun deleteTag(tagId: String) {
        subjectTagDao.deleteTag(tagId)
    }
}
