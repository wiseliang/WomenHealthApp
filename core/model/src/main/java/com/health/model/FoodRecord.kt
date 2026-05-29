package com.health.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodRecord(
    val id: Long = 0,
    val mealType: MealType,
    val photoPath: String? = null,
    val foodName: String,
    val calories: Double,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val fiberG: Double? = null,
    val servingDescription: String? = null,
    val confidence: Double? = null,
    val isManualEntry: Boolean = false,
    val source: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class MealType(val displayName: String) {
    BREAKFAST("早餐"),
    LUNCH("午餐"),
    DINNER("晚餐"),
    SNACK("加餐")
}
