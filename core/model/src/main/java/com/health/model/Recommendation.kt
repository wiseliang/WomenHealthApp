package com.health.model

import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
    val id: Long = 0,
    val cyclePhase: String,
    val category: RecommendationCategory,
    val title: String,
    val summary: String,
    val detailHtml: String? = null,
    val citationKeys: List<String> = emptyList(),
    val priority: Int = 0,
    val applicableFitnessGoals: List<FitnessGoal> = emptyList()
)

@Serializable
enum class RecommendationCategory(val displayName: String) {
    DIET("饮食"),
    EXERCISE("运动"),
    SLEEP("睡眠"),
    GENERAL("综合")
}

@Serializable
enum class FitnessGoal(val displayName: String) {
    FAT_LOSS("减脂"),
    WEIGHT_LOSS("减重"),
    MUSCLE_GAIN("增肌")
}
