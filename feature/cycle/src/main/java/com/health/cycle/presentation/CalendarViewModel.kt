package com.health.cycle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.cycle.domain.algorithm.CyclePredictor
import com.health.cycle.domain.algorithm.PhaseCalculator
import com.health.cycle.domain.repository.CycleRepository
import com.health.cycle.domain.usecase.RecordPeriodUseCase
import com.health.data.dao.UserDao
import com.health.model.CyclePhase
import com.health.model.CyclePrediction
import com.health.model.PeriodRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val periods: List<PeriodRecord> = emptyList(),
    val prediction: CyclePrediction? = null,
    val currentPhase: CyclePhase? = null,
    val cycleDayNow: Int? = null,
    val selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showLogSheet: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val recordPeriodUseCase: RecordPeriodUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var userId: Long = 0

    init {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            if (user != null) {
                userId = user.userId
                cycleRepository.observePeriods(userId).collect { periods ->
                    val prediction = cycleRepository.getLatestPrediction(userId)
                    _uiState.update { state ->
                        state.copy(
                            periods = periods.sortedByDescending { it.startDate },
                            prediction = prediction,
                            currentPhase = prediction?.currentPhase,
                            cycleDayNow = prediction?.cycleDayNow,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onMonthChanged(date: LocalDate) {
        _uiState.update { it.copy(selectedMonth = date.withDayOfMonth(1)) }
    }

    fun showLogSheet() {
        _uiState.update { it.copy(showLogSheet = true) }
    }

    fun hideLogSheet() {
        _uiState.update { it.copy(showLogSheet = false) }
    }

    fun recordPeriod(startDate: LocalDate, endDate: LocalDate, notes: String?) {
        viewModelScope.launch {
            try {
                recordPeriodUseCase(userId, startDate, endDate, notes)
                _uiState.update { it.copy(showLogSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deletePeriod(period: PeriodRecord) {
        viewModelScope.launch {
            cycleRepository.deletePeriod(period)
            cycleRepository.generateAndSavePrediction(userId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
