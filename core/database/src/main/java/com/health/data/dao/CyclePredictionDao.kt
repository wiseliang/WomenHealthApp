package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.CyclePredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CyclePredictionDao {
    @Query("SELECT * FROM cycle_predictions WHERE userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestPrediction(userId: Long): CyclePredictionEntity?

    @Query("SELECT * FROM cycle_predictions WHERE userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    fun observeLatestPrediction(userId: Long): Flow<CyclePredictionEntity?>

    @Query("SELECT * FROM cycle_predictions WHERE userId = :userId ORDER BY calculatedAt DESC LIMIT 12")
    suspend fun getPredictionHistory(userId: Long): List<CyclePredictionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: CyclePredictionEntity): Long
}
