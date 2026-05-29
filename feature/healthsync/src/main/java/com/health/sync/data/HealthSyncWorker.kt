package com.health.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.health.data.dao.HealthSyncRecordDao
import com.health.data.dao.UserDao
import com.health.data.entity.HealthSyncRecordEntity
import com.health.sync.domain.HealthSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class HealthSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthSyncManager: HealthSyncManager,
    private val healthSyncRecordDao: HealthSyncRecordDao,
    private val userDao: UserDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!healthSyncManager.isAuthorized()) return Result.retry()

        return try {
            val user = userDao.getCurrentUser() ?: return Result.failure()
            val now = System.currentTimeMillis()
            val sixHoursAgo = now - 6 * 3600 * 1000

            // Sync steps
            val stepRecords = healthSyncManager.readStepCount(sixHoursAgo, now)
            val stepEntities = stepRecords.map { it.toSyncEntity(user.userId) }

            // Sync weight
            val weightRecords = healthSyncManager.readWeight(sixHoursAgo, now)
            val weightEntities = weightRecords.map { it.toSyncEntity(user.userId) }

            healthSyncRecordDao.insertRecords(stepEntities + weightEntities)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "health_sync_periodic"

        fun schedule() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<HealthSyncWorker>(
                6, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // flex interval
            ).setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance()
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel() {
            WorkManager.getInstance().cancelUniqueWork(WORK_NAME)
        }
    }
}

private fun com.health.model.HealthSyncRecord.toSyncEntity(userId: Long) = HealthSyncRecordEntity(
    userId = userId,
    dataType = dataType.name,
    source = source,
    value = value,
    unit = unit,
    recordedAt = recordedAt,
    syncedAt = syncedAt
)
