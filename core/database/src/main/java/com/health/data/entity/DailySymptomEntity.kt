package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_symptoms",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId", "date", unique = true)]
)
data class DailySymptomEntity(
    @PrimaryKey(autoGenerate = true) val symptomId: Long = 0,
    val userId: Long,
    val date: LocalDate,
    val periodFlow: Int? = null,
    val mood: Int? = null,
    val sleepQuality: Int? = null,
    val skinCondition: Int? = null,
    val cervicalMucus: String? = null,
    val basalBodyTemp: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
