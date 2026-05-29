package com.health.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recommendations",
    indices = [Index("cyclePhase"), Index("category")]
)
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true) val recommendationId: Long = 0,
    val cyclePhase: String,
    val category: String,
    val title: String,
    val summary: String,
    val detailHtml: String? = null,
    val citationKeys: String? = null,
    val priority: Int = 0,
    val applicableFitnessGoals: String? = null
)
