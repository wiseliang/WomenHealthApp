package com.health.recommendation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.cycle.domain.repository.CycleRepository
import com.health.data.dao.UserDao
import com.health.model.Citation
import com.health.model.Recommendation
import com.health.recommendation.domain.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val currentPhase: String = "LUTEAL",
    val fitnessGoal: String? = null,
    val selectedCitation: Citation? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RecommendationListViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val cycleRepository: CycleRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var userId: Long = 0

    init {
        viewModelScope.launch {
            try {
                recommendationRepository.ensureDataLoaded()

                val user = userDao.getCurrentUser()
                if (user != null) {
                    userId = user.userId
                    _uiState.update { it.copy(fitnessGoal = user.fitnessGoal) }
                }

                val prediction = cycleRepository.getLatestPrediction(userId)
                val phase = prediction?.currentPhase?.name ?: "LUTEAL"

                loadRecommendations(phase)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadRecommendations(phase: String) {
        val goal = _uiState.value.fitnessGoal
        val recommendations = recommendationRepository.getRecommendations(phase, goal)
        _uiState.update {
            it.copy(
                recommendations = recommendations,
                currentPhase = phase,
                isLoading = false
            )
        }
    }

    fun showCitation(citationKey: String) {
        viewModelScope.launch {
            val citations = recommendationRepository.getCitations(listOf(citationKey))
            _uiState.update { it.copy(selectedCitation = citations.firstOrNull()) }
        }
    }

    fun hideCitation() {
        _uiState.update { it.copy(selectedCitation = null) }
    }
}
