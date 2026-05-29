package com.health.cycle.domain.repository

import com.health.model.CyclePrediction
import com.health.model.PeriodRecord
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun observePeriods(userId: Long): Flow<List<PeriodRecord>>
    suspend fun getAllPeriods(userId: Long): List<PeriodRecord>
    suspend fun getMostRecentPeriod(userId: Long): PeriodRecord?
    suspend fun recordPeriod(userId: Long, startDate: java.time.LocalDate, endDate: java.time.LocalDate, notes: String? = null)
    suspend fun deletePeriod(period: PeriodRecord)
    suspend fun getLatestPrediction(userId: Long): CyclePrediction?
    suspend fun generateAndSavePrediction(userId: Long): CyclePrediction
}
