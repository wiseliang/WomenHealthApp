package com.health.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

data class DailySymptom(
    val id: Long = 0,
    val date: LocalDate,
    val periodFlow: Int? = null,        // 0=none, 1=spotting, 2=light, 3=medium, 4=heavy
    val mood: Int? = null,              // 1=very low, 5=very high
    val sleepQuality: Int? = null,      // 1=very poor, 5=excellent
    val skinCondition: Int? = null,     // 1=very poor, 5=excellent
    val cervicalMucus: CervicalMucus? = null,
    val basalBodyTemp: Double? = null,  // Celsius
    val notes: String? = null
)

@Serializable
enum class CervicalMucus(val displayName: String) {
    DRY("干燥"),
    STICKY("粘稠"),
    CREAMY("乳状"),
    WATERY("水样"),
    EGG_WHITE("蛋清状")
}
