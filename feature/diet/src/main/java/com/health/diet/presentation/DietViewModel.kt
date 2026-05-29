package com.health.diet.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.data.dao.UserDao
import com.health.diet.data.AssessedFoodItem
import com.health.diet.data.CalorieAssessmentPipeline
import com.health.diet.data.FoodDatabaseService
import com.health.diet.data.FoodNutritionInfo
import com.health.diet.domain.DietRepository
import com.health.model.FoodRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DietUiState(
    val meals: List<FoodRecord> = emptyList(),
    val totalCaloriesToday: Double = 0.0,
    val isCapturing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val assessmentResult: com.health.diet.data.CalorieAssessmentResult? = null,
    val photoPath: String? = null,
    val searchResults: List<FoodNutritionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DietViewModel @Inject constructor(
    private val dietRepository: DietRepository,
    private val assessmentPipeline: CalorieAssessmentPipeline,
    private val foodDatabaseService: FoodDatabaseService,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DietUiState())
    val uiState: StateFlow<DietUiState> = _uiState.asStateFlow()

    private var userId: Long = 0

    init {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            if (user != null) {
                userId = user.userId
                loadTodayData()
            }
        }
    }

    private suspend fun loadTodayData() {
        val today = LocalDate.now()
        dietRepository.observeMealsForDay(userId, today).collect { meals ->
            val total = dietRepository.getTotalCaloriesForDay(userId, today) ?: 0.0
            _uiState.update {
                it.copy(meals = meals.reversed(), totalCaloriesToday = total)
            }
        }
    }

    fun startCapture() {
        _uiState.update { it.copy(isCapturing = true) }
    }

    fun onPhotoCaptured(bitmap: Bitmap, photoPath: String?) {
        _uiState.update { it.copy(capturedBitmap = bitmap, photoPath = photoPath) }
        assessMeal(bitmap)
    }

    private fun assessMeal(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = assessmentPipeline.assess(bitmap)
                _uiState.update { it.copy(assessmentResult = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false,
                        assessmentResult = com.health.diet.data.CalorieAssessmentResult(
                            items = emptyList(), totalCalories = 0.0, anyLowConfidence = false
                        ))
                }
            }
        }
    }

    fun saveMeal(mealType: String, items: List<AssessedFoodItem>) {
        viewModelScope.launch {
            try {
                dietRepository.saveAssessedItems(userId, mealType, _uiState.value.photoPath, items)
                _uiState.update { it.copy(isCapturing = false, capturedBitmap = null, assessmentResult = null, photoPath = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun searchFoods(query: String) {
        viewModelScope.launch {
            val results = foodDatabaseService.search(query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun addManualFood(mealType: String, food: FoodNutritionInfo) {
        viewModelScope.launch {
            val servingG = food.servingSizeG ?: 150.0
            val ratio = servingG / 100.0
            dietRepository.saveFoodRecord(
                userId = userId,
                mealType = mealType,
                foodName = food.foodName,
                calories = food.caloriesPer100g * ratio,
                proteinG = food.proteinPer100g?.times(ratio),
                carbsG = food.carbsPer100g?.times(ratio),
                fatG = food.fatPer100g?.times(ratio),
                fiberG = food.fiberPer100g?.times(ratio),
                servingDesc = "${servingG.toInt()}g",
                photoPath = null,
                confidence = null,
                source = "manual"
            )
        }
    }

    fun cancelAll() {
        _uiState.update { it.copy(isCapturing = false, capturedBitmap = null, assessmentResult = null, photoPath = null) }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
