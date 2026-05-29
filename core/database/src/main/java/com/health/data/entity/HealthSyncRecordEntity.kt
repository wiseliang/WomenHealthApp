package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "health_sync_records",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("dataType"), Index("recordedAt")]
)
data class HealthSyncRecordEntity(
    @PrimaryKey(autoGenerate = true) val syncRecordId: Long = 0,
    val userId: Long,
    val dataType: String,
    val source: String,
    val value: Double,
    val unit: String,
    val recordedAt: LocalDate,
    val syncedAt: Long = System.currentTimeMillis()
)
