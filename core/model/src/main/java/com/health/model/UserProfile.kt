package com.health.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Long = 0,
    val heightCm: Double,
    val weightKg: Double,
    val birthYear: Int,
    val cycleLengthAvg: Int? = null,
    val periodLengthAvg: Int? = null,
    val fitnessGoal: FitnessGoal? = null
) {
    val bmi: Double
        get() {
            val heightM = heightCm / 100.0
            return weightKg / (heightM * heightM)
        }
}
