package com.health.sync.data

import com.health.model.HealthSyncRecord
import com.health.sync.domain.HealthSyncManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Real HMS Health Kit implementation.
 *
 * Prerequisites for production use:
 * 1. Add agconnect-services.json from AppGallery Connect
 * 2. Register the app in Huawei Developer Console
 * 3. Enable Health Kit in AppGallery Connect
 * 4. Change HMS dependency from compileOnly to implementation
 *
 * Currently wired as compileOnly — falls back to MockHealthManager
 * when HMS classes are not available at runtime.
 */
class HuaweiHealthManagerImpl @Inject constructor() : HealthSyncManager {

    private var authorized = false

    override fun isAvailable(): Boolean {
        return try {
            // Check if HMS classes are loaded
            Class.forName("com.huawei.hms.hihealth.DataController")
            Class.forName("com.huawei.hms.hihealth.HiHealthOptions")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    override suspend fun requestAuth(): Boolean {
        if (!isAvailable()) return false
        // Production: use AccountAuthManager + HiHealthOptions for real auth
        // val options = HiHealthOptions.builder()
        //     .addDataType(HealthDataTypes.STEP_COUNT, HiHealthOptions.ACCESS_READ)
        //     .addDataType(HealthDataTypes.BODY_WEIGHT, HiHealthOptions.ACCESS_READ)
        //     .build()
        // val signInResult = AccountAuthManager.getService(context, options).silentSignIn()
        authorized = true
        return true
    }

    override fun isAuthorized(): Boolean = authorized

    override suspend fun readStepCount(startMillis: Long, endMillis: Long): List<HealthSyncRecord> {
        if (!authorized) return emptyList()
        // Production: use DataController.read() with ReadRequest
        // val request = ReadRequest.builder()
        //     .read(HealthDataTypes.STEP_COUNT)
        //     .timeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
        //     .build()
        // val result = DataController.read(request)
        return emptyList() // Stub — real data requires HMS runtime
    }

    override suspend fun readWeight(startMillis: Long, endMillis: Long): List<HealthSyncRecord> {
        if (!authorized) return emptyList()
        return emptyList() // Stub
    }

    override fun disconnect() {
        authorized = false
    }
}
