package com.focusflow.core.data.repository

import com.focusflow.core.common.time.Clock
import com.focusflow.core.data.local.dao.BlockingDao
import com.focusflow.core.data.local.entity.EmergencyPassEntity
import com.focusflow.core.domain.blocking.model.EmergencyPass
import com.focusflow.core.domain.blocking.repository.EmergencyPassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineEmergencyPassRepository @Inject constructor(
    private val blockingDao: BlockingDao,
    private val clock: Clock,
) : EmergencyPassRepository {

    private val todayEpochDay: Long
        get() {
            val instant = Instant.ofEpochMilli(clock.now())
            return LocalDate.ofInstant(instant, ZoneId.systemDefault()).toEpochDay()
        }

    override val emergencyPass: Flow<EmergencyPass> = blockingDao.observeEmergencyPass().map { entity ->
        if (entity == null || entity.dateEpochDay != todayEpochDay) {
            EmergencyPass()
        } else {
            EmergencyPass(
                expiresAtEpochMs = entity.expiresAtEpochMs,
                passesUsedToday = entity.passesUsedToday,
            )
        }
    }

    override suspend fun requestPass(durationMs: Long): Boolean {
        val current = blockingDao.observeEmergencyPass().first()
        val now = clock.now()
        val today = todayEpochDay

        val passesUsed = if (current != null && current.dateEpochDay == today) {
            current.passesUsedToday
        } else {
            0
        }

        val pass = EmergencyPass(passesUsedToday = passesUsed)
        if (!pass.hasPassesRemaining) return false

        val updatedEntity = EmergencyPassEntity(
            id = 1,
            expiresAtEpochMs = now + durationMs,
            passesUsedToday = passesUsed + 1,
            dateEpochDay = today,
        )
        blockingDao.upsertEmergencyPass(updatedEntity)
        return true
    }

    override suspend fun resetDailyPasses() {
        val updated = EmergencyPassEntity(
            id = 1,
            expiresAtEpochMs = 0L,
            passesUsedToday = 0,
            dateEpochDay = todayEpochDay,
        )
        blockingDao.upsertEmergencyPass(updated)
    }
}
