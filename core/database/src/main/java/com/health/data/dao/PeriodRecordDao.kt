package com.health.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.PeriodRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PeriodRecordDao {
    @Query("SELECT * FROM period_records WHERE userId = :userId ORDER BY startDate DESC")
    fun getPeriodsForUser(userId: Long): Flow<List<PeriodRecordEntity>>

    @Query("SELECT * FROM period_records WHERE userId = :userId AND startDate >= :since ORDER BY startDate ASC")
    suspend fun getPeriodsSince(userId: Long, since: LocalDate): List<PeriodRecordEntity>

    @Query("SELECT * FROM period_records WHERE userId = :userId ORDER BY startDate ASC")
    suspend fun getAllPeriodsOrdered(userId: Long): List<PeriodRecordEntity>

    @Query("SELECT * FROM period_records WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    suspend fun getMostRecentPeriod(userId: Long): PeriodRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeriod(period: PeriodRecordEntity): Long

    @Delete
    suspend fun deletePeriod(period: PeriodRecordEntity)
}
