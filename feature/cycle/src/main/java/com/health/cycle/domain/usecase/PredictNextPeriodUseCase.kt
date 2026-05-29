package com.health.cycle.domain.usecase

import com.health.cycle.domain.repository.CycleRepository
import com.health.model.CyclePrediction
import javax.inject.Inject

class PredictNextPeriodUseCase @Inject constructor(
    private val repository: CycleRepository
) {
    suspend operator fun invoke(userId: Long): CyclePrediction {
        return repository.generateAndSavePrediction(userId)
    }
}
