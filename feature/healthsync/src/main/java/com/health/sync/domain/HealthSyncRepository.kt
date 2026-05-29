package com.health.sync.domain

import com.health.model.HealthDataType
import com.health.model.HealthSyncRecord
import java.time.LocalDate

interface HealthSyncRepository {
    fun isAvailable(): Boolean
    fun isAuthorized(): Boolean
    suspend fun connect(): Boolean
    fun disconnect()
    suspend fun syncNow(): Boolean
    suspend fun getStepData(start: LocalDate, end: LocalDate): List<HealthSyncRecord>
    suspend fun getWeightData(start: LocalDate, end: LocalDate): List<HealthSyncRecord>
    suspend fun getLatestSteps(): HealthSyncRecord?
    suspend fun getLatestWeight(): HealthSyncRecord?
}
