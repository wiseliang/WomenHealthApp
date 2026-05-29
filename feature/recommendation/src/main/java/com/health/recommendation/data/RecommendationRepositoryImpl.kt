package com.health.recommendation.data

import com.health.data.DataInitializer
import com.health.data.dao.CitationDao
import com.health.data.dao.RecommendationDao
import com.health.model.Citation
import com.health.model.FitnessGoal
import com.health.model.Recommendation
import com.health.model.RecommendationCategory
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDao: RecommendationDao,
    private val citationDao: CitationDao,
    private val dataInitializer: DataInitializer
) : com.health.recommendation.domain.RecommendationRepository {

    override suspend fun ensureDataLoaded() {
        dataInitializer.initializeIfNeeded()
    }

    override suspend fun getRecommendations(cyclePhase: String, fitnessGoal: String?): List<Recommendation> {
        val entities = if (fitnessGoal != null) {
            recommendationDao.getRecommendationsForPhaseAndGoal(cyclePhase, fitnessGoal)
        } else {
            recommendationDao.getRecommendationsForPhaseOrdered(cyclePhase)
        }
        return entities.sortedBy { it.priority }.map { entity ->
            Recommendation(
                id = entity.recommendationId,
                cyclePhase = entity.cyclePhase,
                category = try {
                    RecommendationCategory.valueOf(entity.category)
                } catch (_: Exception) { RecommendationCategory.GENERAL },
                title = entity.title,
                summary = entity.summary,
                detailHtml = entity.detailHtml,
                citationKeys = entity.citationKeys
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList(),
                priority = entity.priority,
                applicableFitnessGoals = entity.applicableFitnessGoals
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.mapNotNull { try { FitnessGoal.valueOf(it) } catch (_: Exception) { null } }
                    ?: emptyList()
            )
        }
    }

    override suspend fun getCitations(keys: List<String>): List<Citation> {
        return citationDao.getCitations(keys).map { entity ->
            Citation(
                citationKey = entity.citationKey,
                title = entity.title,
                authors = entity.authors,
                journal = entity.journal,
                year = entity.year,
                doi = entity.doi,
                pmid = entity.pmid,
                summarySentence = entity.summarySentence,
                url = entity.url
            )
        }
    }
}
