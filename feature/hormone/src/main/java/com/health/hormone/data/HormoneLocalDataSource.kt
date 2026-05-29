package com.health.hormone.data

import com.health.data.dao.DailySymptomDao
import com.health.data.dao.HormoneAssessmentDao
import com.health.data.entity.DailySymptomEntity
import com.health.data.entity.HormoneAssessmentEntity
import java.time.LocalDate
import javax.inject.Inject

class HormoneLocalDataSource @Inject constructor(
    private val symptomDao: DailySymptomDao,
    private val assessmentDao: HormoneAssessmentDao
) {
    suspend fun getSymptomForDay(userId: Long, date: LocalDate): DailySymptomEntity? =
        symptomDao.getSymptomForDay(userId, date)

    suspend fun getSymptomsInRange(userId: Long, start: LocalDate, end: LocalDate): List<DailySymptomEntity> =
        symptomDao.getSymptomsInRange(userId, start, end)

    suspend fun getRecentSymptoms(userId: Long, limit: Int = 60): List<DailySymptomEntity> =
        symptomDao.getRecentSymptoms(userId, limit)

    suspend fun upsertSymptom(symptom: DailySymptomEntity): Long =
        symptomDao.upsertSymptom(symptom)

    suspend fun getLatestAssessment(userId: Long): HormoneAssessmentEntity? =
        assessmentDao.getRecentAssessments(userId, 1).firstOrNull()

    suspend fun getAssessmentForDate(userId: Long, date: LocalDate): HormoneAssessmentEntity? =
        assessmentDao.getAssessmentForDate(userId, date)

    suspend fun getAssessmentHistory(userId: Long, limit: Int): List<HormoneAssessmentEntity> =
        assessmentDao.getRecentAssessments(userId, limit)

    suspend fun upsertAssessment(assessment: HormoneAssessmentEntity): Long =
        assessmentDao.upsertAssessment(assessment)
}
