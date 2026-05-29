package com.health.data

import android.content.Context
import com.health.data.dao.CitationDao
import com.health.data.dao.RecommendationDao
import com.health.data.entity.CitationEntity
import com.health.data.entity.RecommendationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader

class DataInitializer(
    private val context: Context,
    private val citationDao: CitationDao,
    private val recommendationDao: RecommendationDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        // Check if data already exists
        val existingRecs = recommendationDao.getRecommendationsForPhase("MENSTRUAL")
        if (existingRecs.isNotEmpty()) return@withContext

        loadCitations()
        loadRecommendations()
    }

    private suspend fun loadCitations() {
        val content = readAsset("citations.json")
        val array = json.parseToJsonElement(content).jsonArray
        val entities = array.map { element ->
            val obj = element as JsonObject
            CitationEntity(
                citationKey = obj["citationKey"]!!.jsonPrimitive.content,
                title = obj["title"]!!.jsonPrimitive.content,
                authors = obj["authors"]!!.jsonPrimitive.content,
                journal = obj["journal"]!!.jsonPrimitive.content,
                year = obj["year"]!!.jsonPrimitive.content.toInt(),
                doi = obj["doi"]?.jsonPrimitive?.content,
                pmid = obj["pmid"]?.jsonPrimitive?.content,
                summarySentence = obj["summarySentence"]!!.jsonPrimitive.content,
                url = obj["url"]?.jsonPrimitive?.content
            )
        }
        citationDao.insertAll(entities)
    }

    private suspend fun loadRecommendations() {
        val content = readAsset("recommendations.json")
        val array = json.parseToJsonElement(content).jsonArray
        val entities = array.map { element ->
            val obj = element as JsonObject
            RecommendationEntity(
                cyclePhase = obj["cyclePhase"]!!.jsonPrimitive.content,
                category = obj["category"]!!.jsonPrimitive.content,
                title = obj["title"]!!.jsonPrimitive.content,
                summary = obj["summary"]!!.jsonPrimitive.content,
                detailHtml = obj["detailHtml"]?.jsonPrimitive?.content,
                citationKeys = obj["citationKeys"]?.jsonArray?.joinToString(",") { it.jsonPrimitive.content },
                priority = obj["priority"]!!.jsonPrimitive.content.toInt(),
                applicableFitnessGoals = obj["applicableFitnessGoals"]?.jsonPrimitive?.content
            )
        }
        recommendationDao.insertAll(entities)
    }

    private fun readAsset(fileName: String): String {
        val inputStream = context.assets.open(fileName)
        return BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { it.readText() }
    }
}
