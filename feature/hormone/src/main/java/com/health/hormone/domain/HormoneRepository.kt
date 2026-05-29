package com.health.hormone.domain

import com.health.model.DailySymptom
import com.health.model.HormoneAssessment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HormoneRepository {
    fun observeSymptoms(userId: Long): Flow<List<DailySymptom>>
    suspend fun getSymptomsInRange(userId: Long, start: LocalDate, end: LocalDate): List<DailySymptom>
    suspend fun getSymptomForDay(userId: Long, date: LocalDate): DailySymptom?
    suspend fun saveSymptom(userId: Long, symptom: DailySymptom)
    suspend fun getLatestAssessment(userId: Long): HormoneAssessment?
    suspend fun generateAssessment(userId: Long): HormoneAssessment
    suspend fun getAssessmentHistory(userId: Long, limit: Int): List<HormoneAssessment>
}
