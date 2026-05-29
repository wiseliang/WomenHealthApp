package com.health.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citations")
data class CitationEntity(
    @PrimaryKey val citationKey: String,
    val title: String,
    val authors: String,
    val journal: String,
    val year: Int,
    val doi: String? = null,
    val pmid: String? = null,
    val summarySentence: String,
    val url: String? = null
)
