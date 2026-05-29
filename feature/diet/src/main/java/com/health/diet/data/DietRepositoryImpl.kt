package com.health.diet.data

import com.health.data.dao.FoodRecordDao
import com.health.data.entity.FoodRecordEntity
import com.health.diet.domain.DietRepository
import com.health.model.FoodRecord
import com.health.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class DietRepositoryImpl @Inject constructor(
    private val foodRecordDao: FoodRecordDao
) : DietRepository {

    override fun observeMealsForDay(userId: Long, date: LocalDate): Flow<List<FoodRecord>> {
        val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endOfDay = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000 - 1
        return foodRecordDao.getMealsForDay(userId, startOfDay, endOfDay)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTotalCaloriesForDay(userId: Long, date: LocalDate): Double? {
        val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endOfDay = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000 - 1
        return foodRecordDao.getTotalCaloriesForDay(userId, startOfDay, endOfDay)
    }

    override suspend fun saveFoodRecord(
        userId: Long, mealType: String, foodName: String,
        calories: Double, proteinG: Double?, carbsG: Double?,
        fatG: Double?, fiberG: Double?, servingDesc: String?,
        photoPath: String?, confidence: Double?, source: String?
    ) {
        foodRecordDao.insertFoodRecord(
            FoodRecordEntity(
                userId = userId,
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                fiberG = fiberG,
                servingDescription = servingDesc,
                photoPath = photoPath,
                confidence = confidence,
                isManualEntry = false,
                source = source
            )
        )
    }

    override suspend fun saveAssessedItems(
        userId: Long, mealType: String, photoPath: String?,
        items: List<AssessedFoodItem>
    ) {
        items.forEach { item ->
            foodRecordDao.insertFoodRecord(
                FoodRecordEntity(
                    userId = userId,
                    mealType = mealType,
                    foodName = item.foodName,
                    calories = item.calories,
                    proteinG = item.proteinG,
                    carbsG = item.carbsG,
                    fatG = item.fatG,
                    fiberG = item.fiberG,
                    servingDescription = item.servingDescription,
                    photoPath = photoPath,
                    confidence = item.confidence,
                    isManualEntry = (item.source == "estimated" || item.source == "manual"),
                    source = item.source
                )
            )
        }
    }

    private fun FoodRecordEntity.toDomain() = FoodRecord(
        id = foodRecordId,
        mealType = try { MealType.valueOf(mealType) } catch (_: Exception) { MealType.LUNCH },
        foodName = foodName,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        servingDescription = servingDescription,
        photoPath = photoPath,
        confidence = confidence,
        isManualEntry = isManualEntry,
        source = source,
        createdAt = createdAt
    )
}
