package com.health.sync.data

import com.health.model.HealthSyncRecord
import com.health.sync.domain.HealthSyncManager
import java.time.LocalDate
import javax.inject.Inject

/**
 * Mock implementation for development and non-Huawei devices.
 * Returns simulated step count and weight data for UI demonstration.
 */
class MockHealthManager @Inject constructor() : HealthSyncManager {

    private var authorized = false

    override fun isAvailable(): Boolean = false // Indicates this is mock

    override suspend fun requestAuth(): Boolean {
        authorized = true
        return true
    }

    override fun isAuthorized(): Boolean = authorized

    override suspend fun readStepCount(startMillis: Long, endMillis: Long): List<HealthSyncRecord> {
        // Generate mock step data
        val startDate = LocalDate.ofEpochDay(startMillis / 86400000)
        val endDate = LocalDate.ofEpochDay(endMillis / 86400000)
        val records = mutableListOf<HealthSyncRecord>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            records.add(
                HealthSyncRecord(
                    dataType = com.health.model.HealthDataType.STEP_COUNT,
                    source = "mock_health",
                    value = (3000 + Math.random() * 10000).toLong().toDouble(),
                    unit = "steps",
                    recordedAt = current
                )
            )
            current = current.plusDays(1)
        }
        return records
    }

    override suspend fun readWeight(startMillis: Long, endMillis: Long): List<HealthSyncRecord> {
        val startDate = LocalDate.ofEpochDay(startMillis / 86400000)
        val endDate = LocalDate.ofEpochDay(endMillis / 86400000)
        return listOf(
            HealthSyncRecord(
                dataType = com.health.model.HealthDataType.WEIGHT,
                source = "mock_health",
                value = 55.0 + (Math.random() * 2 - 1),
                unit = "kg",
                recordedAt = endDate
            )
        )
    }

    override fun disconnect() {
        authorized = false
    }
}
