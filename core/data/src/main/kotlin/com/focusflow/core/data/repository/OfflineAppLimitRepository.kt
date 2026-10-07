package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.BlockingDao
import com.focusflow.core.data.local.entity.AppLimitEntity
import com.focusflow.core.domain.blocking.model.DailyAppLimit
import com.focusflow.core.domain.blocking.repository.AppLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineAppLimitRepository @Inject constructor(
    private val blockingDao: BlockingDao,
) : AppLimitRepository {

    override fun observeAllLimits(): Flow<List<DailyAppLimit>> {
        return blockingDao.observeAllLimits().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeLimitForPackage(packageName: String): Flow<DailyAppLimit?> {
        return blockingDao.observeLimitForPackage(packageName).map { it?.toDomain() }
    }

    override suspend fun setLimit(limit: DailyAppLimit) {
        blockingDao.upsertLimit(
            AppLimitEntity(
                packageName = limit.packageName,
                dailyTimeLimitMs = limit.dailyTimeLimitMs,
                dailyLaunchLimit = limit.dailyLaunchLimit,
            )
        )
    }

    override suspend fun removeLimit(packageName: String) {
        blockingDao.deleteLimit(packageName)
    }

    private fun AppLimitEntity.toDomain() = DailyAppLimit(
        packageName = packageName,
        dailyTimeLimitMs = dailyTimeLimitMs,
        dailyLaunchLimit = dailyLaunchLimit,
    )
}
