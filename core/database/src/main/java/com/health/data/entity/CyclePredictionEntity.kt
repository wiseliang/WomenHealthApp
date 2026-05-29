package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "cycle_predictions",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("calculatedAt")]
)
data class CyclePredictionEntity(
    @PrimaryKey(autoGenerate = true) val predictionId: Long = 0,
    val userId: Long,
    val predictedNextPeriodStart: LocalDate,
    val predictedNextPeriodEnd: LocalDate,
    val ovulationDate: LocalDate? = null,
    val fertileWindowStart: LocalDate? = null,
    val fertileWindowEnd: LocalDate? = null,
    val cyclePhase: String? = null,
    val cycleDayNow: Int? = null,
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val modelVersion: String = "v1-mean",
    val calculatedAt: Long = System.currentTimeMillis()
)
