package com.health.hormone.domain.usecase

import com.health.hormone.domain.HormoneRepository
import com.health.model.DailySymptom
import javax.inject.Inject

class LogSymptomUseCase @Inject constructor(
    private val repository: HormoneRepository
) {
    suspend operator fun invoke(userId: Long, symptom: DailySymptom) {
        repository.saveSymptom(userId, symptom)
        repository.generateAssessment(userId)
    }
}
