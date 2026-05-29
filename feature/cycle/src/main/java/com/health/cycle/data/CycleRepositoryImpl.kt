package com.health.cycle.data

import com.health.cycle.domain.algorithm.CyclePredictor
import com.health.cycle.domain.algorithm.PhaseCalculator
import com.health.cycle.domain.repository.CycleRepository
import com.health.data.entity.CyclePredictionEntity
import com.health.data.entity.PeriodRecordEntity
import com.health.model.CyclePrediction
import com.health.model.PeriodRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CycleRepositoryImpl @Inject constructor(
    private val dataSource: CycleLocalDataSource
) : CycleRepository {

    private val predictor = CyclePredictor()

    override fun observePeriods(userId: Long): Flow<List<PeriodRecord>> =
        dataSource.observePeriods(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllPeriods(userId: Long): List<PeriodRecord> =
        dataSource.getAllPeriods(userId).map { it.toDomain() }

    override suspend fun getMostRecentPeriod(userId: Long): PeriodRecord? =
        dataSource.getMostRecentPeriod(userId)?.toDomain()

    override suspend fun recordPeriod(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        notes: String?
    ) {
        dataSource.upsertPeriod(
            PeriodRecordEntity(
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
        )
    }

    override suspend fun deletePeriod(period: PeriodRecord) {
        dataSource.deletePeriod(period.toEntity())
    }

    override suspend fun getLatestPrediction(userId: Long): CyclePrediction? =
        dataSource.getLatestPrediction(userId)?.toDomain()

    override suspend fun generateAndSavePrediction(userId: Long): CyclePrediction {
        val periods = getAllPeriods(userId)
        val prediction = predictor.predict(periods)
        dataSource.insertPrediction(prediction.toEntity(userId))
        return prediction
    }

    // Mapper extensions
    private fun PeriodRecordEntity.toDomain() = PeriodRecord(
        id = recordId,
        startDate = startDate,
        endDate = endDate,
        notes = notes
    )

    private fun PeriodRecord.toEntity() = PeriodRecordEntity(
        recordId = id,
        userId = 0, // will be filled by caller
        startDate = startDate,
        endDate = endDate,
        notes = notes
    )

    private fun CyclePredictionEntity.toDomain() = CyclePrediction(
        id = predictionId,
        predictedNextPeriodStart = predictedNextPeriodStart,
        predictedNextPeriodEnd = predictedNextPeriodEnd,
        ovulationDate = ovulationDate,
        fertileWindowStart = fertileWindowStart,
        fertileWindowEnd = fertileWindowEnd,
        currentPhase = com.health.model.CyclePhase.valueOf(cyclePhase ?: "LUTEAL"),
        cycleDayNow = cycleDayNow,
        averageCycleLength = averageCycleLength,
        averagePeriodLength = averagePeriodLength,
        modelVersion = modelVersion,
        calculatedAt = calculatedAt
    )

    private fun CyclePrediction.toEntity(userId: Long) = CyclePredictionEntity(
        userId = userId,
        predictedNextPeriodStart = predictedNextPeriodStart,
        predictedNextPeriodEnd = predictedNextPeriodEnd,
        ovulationDate = ovulationDate,
        fertileWindowStart = fertileWindowStart,
        fertileWindowEnd = fertileWindowEnd,
        cyclePhase = currentPhase?.name,
        cycleDayNow = cycleDayNow,
        averageCycleLength = averageCycleLength,
        averagePeriodLength = averagePeriodLength,
        modelVersion = modelVersion,
        calculatedAt = calculatedAt
    )
}
