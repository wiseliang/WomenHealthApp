package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.HealthSyncRecordEntity
import java.time.LocalDate

@Dao
interface HealthSyncRecordDao {
    @Query("SELECT * FROM health_sync_records WHERE userId = :userId AND dataType = :dataType AND recordedAt BETWEEN :start AND :end ORDER BY recordedAt")
    suspend fun getRecordsInRange(userId: Long, dataType: String, start: LocalDate, end: LocalDate): List<HealthSyncRecordEntity>

    @Query("SELECT * FROM health_sync_records WHERE userId = :userId AND dataType = :dataType ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestRecord(userId: Long, dataType: String): HealthSyncRecordEntity?

    @Query("SELECT * FROM health_sync_records WHERE userId = :userId ORDER BY recordedAt DESC LIMIT 30")
    suspend fun getRecentRecords(userId: Long): List<HealthSyncRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<HealthSyncRecordEntity>)
}
