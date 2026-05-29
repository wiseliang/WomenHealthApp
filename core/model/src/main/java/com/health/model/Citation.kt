package com.health.model

import kotlinx.serialization.Serializable

@Serializable
data class Citation(
    val citationKey: String,
    val title: String,
    val authors: String,
    val journal: String,
    val year: Int,
    val doi: String? = null,
    val pmid: String? = null,
    val summarySentence: String,
    val url: String? = null
)
