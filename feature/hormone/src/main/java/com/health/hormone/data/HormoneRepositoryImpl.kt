package com.health.hormone.data

import com.health.cycle.domain.repository.CycleRepository
import com.health.data.entity.DailySymptomEntity
import com.health.data.entity.HormoneAssessmentEntity
import com.health.hormone.domain.HormoneEstimator
import com.health.hormone.domain.HormoneRepository
import com.health.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class HormoneRepositoryImpl @Inject constructor(
    private val dataSource: HormoneLocalDataSource,
    private val cycleRepository: CycleRepository
) : HormoneRepository {

    private val estimator = HormoneEstimator()

    override fun observeSymptoms(userId: Long): Flow<List<DailySymptom>> {
        // Not directly from DAO (no Flow in symptom DAO for recent), use another approach
        // Return empty for now; assessment-based queries are suspend
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override suspend fun getSymptomsInRange(userId: Long, start: LocalDate, end: LocalDate): List<DailySymptom> =
        dataSource.getSymptomsInRange(userId, start, end).map { it.toDomain() }

    override suspend fun getSymptomForDay(userId: Long, date: LocalDate): DailySymptom? =
        dataSource.getSymptomForDay(userId, date)?.toDomain()

    override suspend fun saveSymptom(userId: Long, symptom: DailySymptom) {
        dataSource.upsertSymptom(symptom.toEntity(userId))
    }

    override suspend fun getLatestAssessment(userId: Long): HormoneAssessment? =
        dataSource.getLatestAssessment(userId)?.toDomain()

    override suspend fun generateAssessment(userId: Long): HormoneAssessment {
        val symptoms = dataSource.getRecentSymptoms(userId).map { it.toDomain() }
        val periods = cycleRepository.getAllPeriods(userId)
        val today = LocalDate.now()

        val lastPeriod = cycleRepository.getMostRecentPeriod(userId)
        val prediction = cycleRepository.getLatestPrediction(userId)
        val avgCycleLength = prediction?.averageCycleLength ?: 28

        val (cyclePhase, cycleDay) = if (lastPeriod != null) {
            val day = com.health.cycle.domain.algorithm.PhaseCalculator.currentCycleDay(lastPeriod.startDate, today)
            val phase = com.health.cycle.domain.algorithm.PhaseCalculator.determinePhase(day, avgCycleLength)
            phase to day
        } else {
            CyclePhase.LUTEAL to 14
        }

        val assessment = estimator.assess(symptoms, cyclePhase, cycleDay, avgCycleLength)

        dataSource.upsertAssessment(assessment.toEntity(userId))
        return assessment
    }

    override suspend fun getAssessmentHistory(userId: Long, limit: Int): List<HormoneAssessment> =
        dataSource.getAssessmentHistory(userId, limit).map { it.toDomain() }

    // Mappers
    private fun DailySymptomEntity.toDomain() = DailySymptom(
        id = symptomId,
        date = date,
        periodFlow = periodFlow,
        mood = mood,
        sleepQuality = sleepQuality,
        skinCondition = skinCondition,
        cervicalMucus = cervicalMucus?.let { CervicalMucus.valueOf(it.uppercase()) },
        basalBodyTemp = basalBodyTemp,
        notes = notes
    )

    private fun DailySymptom.toEntity(userId: Long) = DailySymptomEntity(
        symptomId = id,
        userId = userId,
        date = date,
        periodFlow = periodFlow,
        mood = mood,
        sleepQuality = sleepQuality,
        skinCondition = skinCondition,
        cervicalMucus = cervicalMucus?.name?.lowercase(),
        basalBodyTemp = basalBodyTemp,
        notes = notes
    )

    private fun HormoneAssessmentEntity.toDomain() = HormoneAssessment(
        id = assessmentId,
        date = date,
        estimatedEstrogen = estimatedEstrogenLevel,
        estimatedProgesterone = estimatedProgesteroneLevel,
        estrogenClass = estrogenClass?.let { HormoneLevel.valueOf(it) },
        progesteroneClass = progesteroneClass?.let { HormoneLevel.valueOf(it) },
        confidenceScore = confidenceScore ?: 0.0,
        assessmentMethod = assessmentMethod
    )

    private fun HormoneAssessment.toEntity(userId: Long) = HormoneAssessmentEntity(
        userId = userId,
        date = date,
        estimatedEstrogenLevel = estimatedEstrogen,
        estimatedProgesteroneLevel = estimatedProgesterone,
        estrogenClass = estrogenClass?.name,
        progesteroneClass = progesteroneClass?.name,
        confidenceScore = confidenceScore,
        assessmentMethod = assessmentMethod
    )
}
