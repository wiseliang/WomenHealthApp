package com.health.hormone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.data.dao.UserDao
import com.health.hormone.domain.HormoneRepository
import com.health.hormone.domain.usecase.LogSymptomUseCase
import com.health.model.DailySymptom
import com.health.model.HormoneAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HormoneDashboardUiState(
    val latestAssessment: HormoneAssessment? = null,
    val assessmentHistory: List<HormoneAssessment> = emptyList(),
    val todaysSymptom: DailySymptom? = null,
    val isLoading: Boolean = true,
    val showLogSheet: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HormoneDashboardViewModel @Inject constructor(
    private val hormoneRepository: HormoneRepository,
    private val logSymptomUseCase: LogSymptomUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HormoneDashboardUiState())
    val uiState: StateFlow<HormoneDashboardUiState> = _uiState.asStateFlow()

    private var userId: Long = 0

    init {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            if (user != null) {
                userId = user.userId
                loadData()
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val assessment = hormoneRepository.generateAssessment(userId)
            val history = hormoneRepository.getAssessmentHistory(userId, 30)
            val todaySymptom = hormoneRepository.getSymptomForDay(userId, java.time.LocalDate.now())
            _uiState.update {
                it.copy(
                    latestAssessment = assessment,
                    assessmentHistory = history,
                    todaysSymptom = todaySymptom,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun showLogSheet() { _uiState.update { it.copy(showLogSheet = true) } }
    fun hideLogSheet() { _uiState.update { it.copy(showLogSheet = false) } }

    fun saveSymptom(symptom: DailySymptom) {
        viewModelScope.launch {
            try {
                logSymptomUseCase(userId, symptom)
                _uiState.update { it.copy(showLogSheet = false) }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { loadData() }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
