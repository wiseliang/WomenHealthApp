package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.RecommendationEntity

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE cyclePhase = :phase")
    suspend fun getRecommendationsForPhase(phase: String): List<RecommendationEntity>

    @Query("SELECT * FROM recommendations WHERE cyclePhase = :phase AND (applicableFitnessGoals IS NULL OR applicableFitnessGoals LIKE '%' || :goal || '%')")
    suspend fun getRecommendationsForPhaseAndGoal(phase: String, goal: String): List<RecommendationEntity>

    @Query("SELECT * FROM recommendations WHERE cyclePhase = :phase ORDER BY priority ASC")
    suspend fun getRecommendationsForPhaseOrdered(phase: String): List<RecommendationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recommendations: List<RecommendationEntity>)

    @Query("DELETE FROM recommendations")
    suspend fun deleteAll()
}
