package com.health.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_database_cache")
data class FoodDatabaseEntryEntity(
    @PrimaryKey val foodId: String,
    val foodName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val fiberPer100g: Double? = null,
    val servingSizeG: Double? = null,
    val category: String? = null,
    val source: String,
    val lastQueriedAt: Long = System.currentTimeMillis()
)
