package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "period_records",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("startDate")]
)
data class PeriodRecordEntity(
    @PrimaryKey(autoGenerate = true) val recordId: Long = 0,
    val userId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
