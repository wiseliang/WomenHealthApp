package com.health.sync.domain

import com.health.model.HealthSyncRecord

interface HealthSyncManager {
    /** Check if the health service is available on this device */
    fun isAvailable(): Boolean

    /** Request authorization to read health data */
    suspend fun requestAuth(): Boolean

    /** Whether the user has granted permission */
    fun isAuthorized(): Boolean

    /** Read step count for a date range */
    suspend fun readStepCount(startMillis: Long, endMillis: Long): List<HealthSyncRecord>

    /** Read weight records for a date range */
    suspend fun readWeight(startMillis: Long, endMillis: Long): List<HealthSyncRecord>

    /** Disconnect and release resources */
    fun disconnect()
}
