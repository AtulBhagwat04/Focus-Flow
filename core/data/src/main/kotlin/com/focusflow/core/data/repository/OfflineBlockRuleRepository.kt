package com.focusflow.core.data.repository

import com.focusflow.core.data.local.dao.BlockingDao
import com.focusflow.core.data.local.entity.BlockRuleEntity
import com.focusflow.core.data.local.entity.BlockScheduleEntity
import com.focusflow.core.domain.blocking.model.AppBlockRule
import com.focusflow.core.domain.blocking.model.BlockSchedule
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineBlockRuleRepository @Inject constructor(
    private val blockingDao: BlockingDao,
) : BlockRuleRepository {

    override fun observeAllRules(): Flow<List<AppBlockRule>> {
        return blockingDao.observeAllRules().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeRuleForPackage(packageName: String): Flow<AppBlockRule?> {
        return blockingDao.observeRuleForPackage(packageName).map { it?.toDomain() }
    }

    override suspend fun setRule(rule: AppBlockRule) {
        blockingDao.upsertRule(
            BlockRuleEntity(
                packageName = rule.packageName,
                blockDuringFocus = rule.blockDuringFocus,
                isAlwaysBlocked = rule.isAlwaysBlocked,
            )
        )
    }

    override suspend fun removeRule(packageName: String) {
        blockingDao.deleteRule(packageName)
    }

    override fun observeAllSchedules(): Flow<List<BlockSchedule>> {
        return blockingDao.observeAllSchedules().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun upsertSchedule(schedule: BlockSchedule) {
        blockingDao.upsertSchedule(
            BlockScheduleEntity(
                id = schedule.id,
                name = schedule.name,
                daysOfWeekCsv = schedule.daysOfWeek.joinToString(","),
                startMinuteOfDay = schedule.startMinuteOfDay,
                endMinuteOfDay = schedule.endMinuteOfDay,
                packageNamesCsv = schedule.packageNames.joinToString(","),
                isEnabled = schedule.isEnabled,
            )
        )
    }

    override suspend fun deleteSchedule(scheduleId: String) {
        blockingDao.deleteSchedule(scheduleId)
    }

    private fun BlockRuleEntity.toDomain() = AppBlockRule(
        packageName = packageName,
        blockDuringFocus = blockDuringFocus,
        isAlwaysBlocked = isAlwaysBlocked,
    )

    private fun BlockScheduleEntity.toDomain(): BlockSchedule {
        val days = daysOfWeekCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        val pkgs = packageNamesCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        return BlockSchedule(
            id = id,
            name = name,
            daysOfWeek = days,
            startMinuteOfDay = startMinuteOfDay,
            endMinuteOfDay = endMinuteOfDay,
            packageNames = pkgs,
            isEnabled = isEnabled,
        )
    }
}
