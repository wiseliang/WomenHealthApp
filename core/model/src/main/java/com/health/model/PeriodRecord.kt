package com.health.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class PeriodRecord(
    val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String? = null
) {
    val durationDays: Int
        get() = startDate.until(endDate).days + 1
}
