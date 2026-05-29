package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_records",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("createdAt")]
)
data class FoodRecordEntity(
    @PrimaryKey(autoGenerate = true) val foodRecordId: Long = 0,
    val userId: Long,
    val mealType: String,
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
