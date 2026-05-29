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
 * Calorie assessment pipeline.
 * Currently uses manual food entry (ML Kit unavailable in local mirrors).
 * Always returns empty result → UI shows manual entry flow.
 */
class CalorieAssessmentPipeline @Inject constructor(
    private val foodDatabaseService: FoodDatabaseService
) {
    suspend fun assess(bitmap: Bitmap): CalorieAssessmentResult {
        // ML Kit removed — direct to manual entry
        return CalorieAssessmentResult(
            items = emptyList(),
            totalCalories = 0.0,
            anyLowConfidence = false
        )
    }
}
