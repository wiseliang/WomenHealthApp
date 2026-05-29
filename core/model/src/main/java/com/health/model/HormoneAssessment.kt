package com.health.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

data class HormoneAssessment(
    val id: Long = 0,
    val date: LocalDate,
    val estimatedEstrogen: Double?,     // Normalized 0.0-1.0
    val estimatedProgesterone: Double?, // Normalized 0.0-1.0
    val estrogenClass: HormoneLevel?,
    val progesteroneClass: HormoneLevel?,
    val confidenceScore: Double,        // 0.0-1.0
    val assessmentMethod: String = "symptom_scoring"
)

@Serializable
enum class HormoneLevel(val displayName: String) {
    LOW("偏低"),
    NORMAL("正常"),
    HIGH("偏高")
}
