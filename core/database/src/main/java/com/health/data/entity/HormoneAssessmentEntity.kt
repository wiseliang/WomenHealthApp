package com.health.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "hormone_assessments",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId"), Index("date", unique = true)]
)
data class HormoneAssessmentEntity(
    @PrimaryKey(autoGenerate = true) val assessmentId: Long = 0,
    val userId: Long,
    val date: LocalDate,
    val estimatedEstrogenLevel: Double? = null,
    val estimatedProgesteroneLevel: Double? = null,
    val estrogenClass: String? = null,
    val progesteroneClass: String? = null,
    val confidenceScore: Double? = null,
    val assessmentMethod: String = "symptom_scoring",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
