package com.health.sync.data

import com.health.data.dao.HealthSyncRecordDao
import com.health.data.dao.UserDao
import com.health.model.HealthDataType
import com.health.model.HealthSyncRecord
import com.health.sync.domain.HealthSyncManager
import com.health.sync.domain.HealthSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class HealthSyncRepositoryImpl @Inject constructor(
    private val healthSyncManager: HealthSyncManager,
    private val healthSyncRecordDao: HealthSyncRecordDao,
    private val userDao: UserDao
) : HealthSyncRepository {

    private var syncEnabled = false

    override fun isAvailable(): Boolean = healthSyncManager.isAvailable()

    override fun isAuthorized(): Boolean = healthSyncManager.isAuthorized()

    override suspend fun connect(): Boolean {
        val result = healthSyncManager.requestAuth()
        if (result) {
            syncEnabled = true
        }
        return result
    }

    override fun disconnect() {
        syncEnabled = false
        healthSyncManager.disconnect()
    }

    override suspend fun syncNow(): Boolean {
        if (!healthSyncManager.isAuthorized()) return false
        val user = userDao.getCurrentUser() ?: return false

        return try {
            val now = System.currentTimeMillis()
            val weekAgo = now - 7 * 86400 * 1000

            val stepRecords = healthSyncManager.readStepCount(weekAgo, now)
            val weightRecords = healthSyncManager.readWeight(weekAgo, now)

            val stepEntities = stepRecords.map {
                com.health.data.entity.HealthSyncRecordEntity(
                    userId = user.userId, dataType = "STEP_COUNT",
                    source = it.source, value = it.value, unit = it.unit,
                    recordedAt = it.recordedAt
                )
            }
            val weightEntities = weightRecords.map {
                com.health.data.entity.HealthSyncRecordEntity(
                    userId = user.userId, dataType = "WEIGHT",
                    source = it.source, value = it.value, unit = it.unit,
                    recordedAt = it.recordedAt
                )
            }
            healthSyncRecordDao.insertRecords(stepEntities + weightEntities)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getStepData(start: LocalDate, end: LocalDate): List<HealthSyncRecord> {
        val user = userDao.getCurrentUser() ?: return emptyList()
        return healthSyncRecordDao.getRecordsInRange(user.userId, "STEP_COUNT", start, end)
            .map { it.toDomain() }
    }

    override suspend fun getWeightData(start: LocalDate, end: LocalDate): List<HealthSyncRecord> {
        val user = userDao.getCurrentUser() ?: return emptyList()
        return healthSyncRecordDao.getRecordsInRange(user.userId, "WEIGHT", start, end)
            .map { it.toDomain() }
    }

    override suspend fun getLatestSteps(): HealthSyncRecord? {
        val user = userDao.getCurrentUser() ?: return null
        return healthSyncRecordDao.getLatestRecord(user.userId, "STEP_COUNT")?.toDomain()
    }

    override suspend fun getLatestWeight(): HealthSyncRecord? {
        val user = userDao.getCurrentUser() ?: return null
        return healthSyncRecordDao.getLatestRecord(user.userId, "WEIGHT")?.toDomain()
    }

    private fun com.health.data.entity.HealthSyncRecordEntity.toDomain() = HealthSyncRecord(
        id = syncRecordId,
        dataType = try { HealthDataType.valueOf(dataType) } catch (_: Exception) { HealthDataType.STEP_COUNT },
        source = source,
        value = value,
        unit = unit,
        recordedAt = recordedAt,
        syncedAt = syncedAt
    )
}
