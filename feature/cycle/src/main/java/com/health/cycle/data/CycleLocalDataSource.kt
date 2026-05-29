package com.health.cycle.data

import com.health.data.dao.CyclePredictionDao
import com.health.data.dao.PeriodRecordDao
import com.health.data.entity.CyclePredictionEntity
import com.health.data.entity.PeriodRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CycleLocalDataSource @Inject constructor(
    private val periodRecordDao: PeriodRecordDao,
    private val cyclePredictionDao: CyclePredictionDao
) {
    fun observePeriods(userId: Long): Flow<List<PeriodRecordEntity>> =
        periodRecordDao.getPeriodsForUser(userId)

    suspend fun getAllPeriods(userId: Long): List<PeriodRecordEntity> =
        periodRecordDao.getAllPeriodsOrdered(userId)

    suspend fun getMostRecentPeriod(userId: Long): PeriodRecordEntity? =
        periodRecordDao.getMostRecentPeriod(userId)

    suspend fun upsertPeriod(period: PeriodRecordEntity): Long =
        periodRecordDao.upsertPeriod(period)

    suspend fun deletePeriod(period: PeriodRecordEntity) =
        periodRecordDao.deletePeriod(period)

    suspend fun getLatestPrediction(userId: Long): CyclePredictionEntity? =
        cyclePredictionDao.getLatestPrediction(userId)

    fun observeLatestPrediction(userId: Long): Flow<CyclePredictionEntity?> =
        cyclePredictionDao.observeLatestPrediction(userId)

    suspend fun insertPrediction(prediction: CyclePredictionEntity): Long =
        cyclePredictionDao.insertPrediction(prediction)
}
