package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.FoodRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodRecordDao {
    @Query("SELECT * FROM food_records WHERE userId = :userId AND createdAt BETWEEN :startOfDay AND :endOfDay ORDER BY createdAt DESC")
    fun getMealsForDay(userId: Long, startOfDay: Long, endOfDay: Long): Flow<List<FoodRecordEntity>>

    @Query("SELECT SUM(calories) FROM food_records WHERE userId = :userId AND createdAt BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTotalCaloriesForDay(userId: Long, startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT * FROM food_records WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentMeals(userId: Long, limit: Int = 20): Flow<List<FoodRecordEntity>>

    @Query("SELECT * FROM food_records WHERE userId = :userId AND createdAt BETWEEN :startMillis AND :endMillis ORDER BY createdAt DESC")
    suspend fun getMealsInRange(userId: Long, startMillis: Long, endMillis: Long): List<FoodRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodRecord(record: FoodRecordEntity): Long
}
