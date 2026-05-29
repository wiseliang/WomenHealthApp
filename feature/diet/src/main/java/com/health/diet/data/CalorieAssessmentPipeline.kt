package com.health.diet.data

import android.graphics.Bitmap
import javax.inject.Inject

data class AssessedFoodItem(
    val foodName: String,
    val calories: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val fiberG: Double? = null,
    val servingSizeG: Double? = null,
    val servingDescription: String = "1份",
    val confidence: Double = 0.0,
    val source: String = "unknown"
)

data class CalorieAssessmentResult(
    val items: List<AssessedFoodItem>,
    val totalCalories: Double,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val anyLowConfidence: Boolean = false
)

/**
 * Pipeline: Photo → ML Kit Detection → Food DB Lookup → Portion Estimate → Aggregate.
 * On ML Kit failure, returns empty result for manual entry.
 */
class CalorieAssessmentPipeline @Inject constructor(
    private val foodDetectionService: FoodDetectionService,
    private val foodDatabaseService: FoodDatabaseService
) {
    suspend fun assess(bitmap: Bitmap): CalorieAssessmentResult {
        // Step 1: Detect food items
        val detectionResult = foodDetectionService.detect(bitmap)
        val detectedFoods = detectionResult.getOrDefault(emptyList())

        if (detectedFoods.isEmpty()) {
            return CalorieAssessmentResult(
                items = emptyList(),
                totalCalories = 0.0,
                anyLowConfidence = false
            )
        }

        // Step 2: Look up each food in database
        val items = detectedFoods.map { detected ->
            val nutrition = foodDatabaseService.lookup(detected.name)

            if (nutrition != null) {
                val servingG = nutrition.servingSizeG ?: 150.0
                val ratio = servingG / 100.0
                AssessedFoodItem(
                    foodName = nutrition.foodName,
                    calories = (nutrition.caloriesPer100g * ratio).let { Math.round(it * 10) / 10.0 },
                    proteinG = nutrition.proteinPer100g?.times(ratio)?.let { Math.round(it * 10) / 10.0 },
                    carbsG = nutrition.carbsPer100g?.times(ratio)?.let { Math.round(it * 10) / 10.0 },
                    fatG = nutrition.fatPer100g?.times(ratio)?.let { Math.round(it * 10) / 10.0 },
                    fiberG = nutrition.fiberPer100g?.times(ratio)?.let { Math.round(it * 10) / 10.0 },
                    servingSizeG = servingG,
                    servingDescription = if (servingG >= 100) "${servingG.toInt()}g" else "1份",
                    confidence = detected.confidence.toDouble(),
                    source = nutrition.source
                )
            } else {
                AssessedFoodItem(
                    foodName = detected.name,
                    calories = 200.0, // Generic estimate
                    servingDescription = "1份",
                    confidence = detected.confidence.toDouble(),
                    source = "estimated"
                )
            }
        }

        // Step 3: Aggregate
        val totalCal = items.sumOf { it.calories }
        val totalProtein = items.mapNotNull { it.proteinG }.sum()
        val totalCarbs = items.mapNotNull { it.carbsG }.sum()
        val totalFat = items.mapNotNull { it.fatG }.sum()
        val totalFiber = items.mapNotNull { it.fiberG }.sum()
        val lowConfidence = items.any { it.confidence < 0.7 }

        return CalorieAssessmentResult(
            items = items,
            totalCalories = Math.round(totalCal * 10) / 10.0,
            totalProtein = Math.round(totalProtein * 10) / 10.0,
            totalCarbs = Math.round(totalCarbs * 10) / 10.0,
            totalFat = Math.round(totalFat * 10) / 10.0,
            totalFiber = Math.round(totalFiber * 10) / 10.0,
            anyLowConfidence = lowConfidence
        )
    }
}
