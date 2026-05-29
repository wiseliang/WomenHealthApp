package com.health.recommendation.domain

import com.health.model.Citation
import com.health.model.Recommendation

interface RecommendationRepository {
    suspend fun getRecommendations(cyclePhase: String, fitnessGoal: String?): List<Recommendation>
    suspend fun getCitations(keys: List<String>): List<Citation>
    suspend fun ensureDataLoaded()
}
