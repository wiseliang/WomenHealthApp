package com.health.profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.common.DataExporter
import com.health.data.dao.UserDao
import com.health.data.entity.UserEntity
import com.health.model.HealthSyncRecord
import com.health.sync.domain.HealthSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProfileUiState(
    val heightCm: Double = 165.0,
    val weightKg: Double = 55.0,
    val birthYear: Int = 1995,
    val fitnessGoal: String? = null,
    val isHmsAvailable: Boolean = false,
    val isHmsConnected: Boolean = false,
    val latestSteps: HealthSyncRecord? = null,
    val latestWeight: HealthSyncRecord? = null,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val healthSyncRepository: HealthSyncRepository,
    private val dataExporter: DataExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        heightCm = user.heightCm,
                        weightKg = user.weightKg,
                        birthYear = user.birthYear,
                        fitnessGoal = user.fitnessGoal
                    )
                }
            }
            _uiState.update {
                it.copy(
                    isHmsAvailable = healthSyncRepository.isAvailable(),
                    isHmsConnected = healthSyncRepository.isAuthorized()
                )
            }
            if (healthSyncRepository.isAuthorized()) {
                loadSyncData()
            }
        }
    }

    private suspend fun loadSyncData() {
        _uiState.update { it.copy(isSyncing = true) }
        val steps = healthSyncRepository.getLatestSteps()
        val weight = healthSyncRepository.getLatestWeight()
        _uiState.update {
            it.copy(
                latestSteps = steps,
                latestWeight = weight,
                isSyncing = false,
                lastSyncTime = steps?.syncedAt ?: weight?.syncedAt
            )
        }
    }

    fun saveProfile(height: Double, weight: Double, birthYear: Int, goal: String?) {
        viewModelScope.launch {
            userDao.upsertUser(
                UserEntity(
                    userId = userDao.getCurrentUser()?.userId ?: 0,
                    heightCm = height,
                    weightKg = weight,
                    birthYear = birthYear,
                    fitnessGoal = goal
                )
            )
            _uiState.update {
                it.copy(heightCm = height, weightKg = weight,
                    birthYear = birthYear, fitnessGoal = goal)
            }
        }
    }

    fun connectHuaweiHealth() {
        viewModelScope.launch {
            val success = healthSyncRepository.connect()
            _uiState.update { it.copy(isHmsConnected = success) }
            if (success) loadSyncData()
        }
    }

    fun disconnectHuaweiHealth() {
        healthSyncRepository.disconnect()
        _uiState.update {
            it.copy(isHmsConnected = false, latestSteps = null, latestWeight = null)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            healthSyncRepository.syncNow()
            loadSyncData()
        }
    }

    private var exportFile: File? = null

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val file = dataExporter.exportCsv(context)
                exportFile = file
                dataExporter.shareFile(context, file)
            } catch (_: Exception) { }
        }
    }
}
