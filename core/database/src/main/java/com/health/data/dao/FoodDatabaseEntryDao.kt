package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.FoodDatabaseEntryEntity

@Dao
interface FoodDatabaseEntryDao {
    @Query("SELECT * FROM food_database_cache WHERE foodName LIKE '%' || :query || '%'")
    suspend fun searchFoods(query: String): List<FoodDatabaseEntryEntity>

    @Query("SELECT * FROM food_database_cache WHERE foodId = :foodId")
    suspend fun getFood(foodId: String): FoodDatabaseEntryEntity?

    @Query("SELECT * FROM food_database_cache WHERE lastQueriedAt < :staleBefore LIMIT :limit")
    suspend fun getStaleEntries(staleBefore: Long, limit: Int = 50): List<FoodDatabaseEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FoodDatabaseEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodDatabaseEntryEntity)
}
