package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.HormoneAssessmentEntity
import java.time.LocalDate

@Dao
interface HormoneAssessmentDao {
    @Query("SELECT * FROM hormone_assessments WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentAssessments(userId: Long, limit: Int = 7): List<HormoneAssessmentEntity>

    @Query("SELECT * FROM hormone_assessments WHERE userId = :userId AND date = :date")
    suspend fun getAssessmentForDate(userId: Long, date: LocalDate): HormoneAssessmentEntity?

    @Query("SELECT * FROM hormone_assessments WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    suspend fun getAssessmentHistory(userId: Long): List<HormoneAssessmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssessment(assessment: HormoneAssessmentEntity): Long
}
