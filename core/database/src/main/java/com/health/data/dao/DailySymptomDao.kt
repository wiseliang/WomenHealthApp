package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.DailySymptomEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailySymptomDao {
    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId AND date = :date")
    suspend fun getSymptomForDay(userId: Long, date: LocalDate): DailySymptomEntity?

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId ORDER BY date DESC")
    fun getSymptomFlowForUser(userId: Long): Flow<List<DailySymptomEntity>>

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getSymptomsInRange(userId: Long, start: LocalDate, end: LocalDate): List<DailySymptomEntity>

    @Query("SELECT * FROM daily_symptoms WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSymptoms(userId: Long, limit: Int = 60): List<DailySymptomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSymptom(symptom: DailySymptomEntity): Long
}
