package com.health.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val heightCm: Double,
    val weightKg: Double,
    val birthYear: Int,
    val cycleLengthAvg: Int? = null,
    val periodLengthAvg: Int? = null,
    val fitnessGoal: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
