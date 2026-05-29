package com.health.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class HealthSyncRecord(
    val id: Long = 0,
    val dataType: HealthDataType,
    val source: String = "huawei_health",
    val value: Double,
    val unit: String,
    val recordedAt: LocalDate,
    val syncedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class HealthDataType(val displayName: String) {
    STEP_COUNT("步数"),
    WEIGHT("体重"),
    EXERCISE("运动时长")
}
