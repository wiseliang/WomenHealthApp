package com.health.onboarding.presentation

import androidx.lifecycle.ViewModel
import com.health.data.dao.UserDao
import com.health.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    var heightCm: Double = 165.0
    var weightKg: Double = 55.0
    var birthYear: Int = 1995
    var fitnessGoal: String? = null
    var huaweiConnectDesired: Boolean = false

    suspend fun saveProfile() {
        userDao.upsertUser(
            UserEntity(
                heightCm = heightCm,
                weightKg = weightKg,
                birthYear = birthYear,
                fitnessGoal = fitnessGoal
            )
        )
    }
}
