package com.health.cycle.domain.usecase

import com.health.cycle.domain.repository.CycleRepository
import com.health.model.PeriodRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCycleHistoryUseCase @Inject constructor(
    private val repository: CycleRepository
) {
    operator fun invoke(userId: Long): Flow<List<PeriodRecord>> {
        return repository.observePeriods(userId)
    }
}
