package com.health.diet.domain

import com.health.diet.data.AssessedFoodItem
import com.health.model.FoodRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DietRepository {
    fun observeMealsForDay(userId: Long, date: LocalDate): Flow<List<FoodRecord>>
    suspend fun getTotalCaloriesForDay(userId: Long, date: LocalDate): Double?
    suspend fun saveFoodRecord(userId: Long, mealType: String, foodName: String,
                              calories: Double, proteinG: Double?, carbsG: Double?,
                              fatG: Double?, fiberG: Double?, servingDesc: String?,
                              photoPath: String?, confidence: Double?, source: String?)
    suspend fun saveAssessedItems(userId: Long, mealType: String, photoPath: String?,
                                   items: List<AssessedFoodItem>)
}
