package com.health.cycle.domain.usecase

import com.health.cycle.domain.algorithm.PhaseCalculator
import com.health.cycle.domain.repository.CycleRepository
import com.health.model.CyclePhase
import java.time.LocalDate
import javax.inject.Inject

class GetCurrentCyclePhaseUseCase @Inject constructor(
    private val repository: CycleRepository
) {
    suspend operator fun invoke(userId: Long): CyclePhase? {
        val lastPeriod = repository.getMostRecentPeriod(userId) ?: return null
        val cycleDay = PhaseCalculator.currentCycleDay(lastPeriod.startDate, LocalDate.now())
        val prediction = repository.getLatestPrediction(userId)
        val avgCycleLength = prediction?.averageCycleLength ?: 28
        return PhaseCalculator.determinePhase(cycleDay, avgCycleLength)
    }
}
