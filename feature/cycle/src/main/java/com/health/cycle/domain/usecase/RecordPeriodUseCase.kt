package com.health.cycle.domain.usecase

import com.health.cycle.domain.repository.CycleRepository
import java.time.LocalDate
import javax.inject.Inject

class RecordPeriodUseCase @Inject constructor(
    private val repository: CycleRepository
) {
    suspend operator fun invoke(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        notes: String? = null
    ) {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
        require(endDate.until(startDate).days >= 0) { "Period must be at least 1 day" }
        repository.recordPeriod(userId, startDate, endDate, notes)
        repository.generateAndSavePrediction(userId)
    }
}
