package com.health.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodDatabaseEntry(
    val foodId: String,
    val foodName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val fiberPer100g: Double? = null,
    val servingSizeG: Double? = null,
    val category: String? = null,
    val source: String
)
