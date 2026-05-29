package com.health.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CyclePrediction(
    val id: Long = 0,
    val predictedNextPeriodStart: LocalDate,
    val predictedNextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate? = null,
    val fertileWindowStart: LocalDate? = null,
    val fertileWindowEnd: LocalDate? = null,
    val currentPhase: CyclePhase? = null,
    val cycleDayNow: Int? = null,
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val modelVersion: String = "v1-mean",
    val calculatedAt: Long = System.currentTimeMillis()
)
