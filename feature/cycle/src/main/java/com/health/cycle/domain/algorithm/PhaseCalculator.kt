package com.health.cycle.domain.algorithm

import com.health.model.CyclePhase

object PhaseCalculator {

    fun determinePhase(cycleDay: Int, averageCycleLength: Int = 28): CyclePhase {
        val effectiveDay = ((cycleDay - 1) % averageCycleLength) + 1
        return when {
            effectiveDay <= 5 -> CyclePhase.MENSTRUAL
            effectiveDay <= averageCycleLength / 2 -> CyclePhase.FOLLICULAR
            effectiveDay <= (averageCycleLength / 2) + 2 -> CyclePhase.OVULATORY
            else -> CyclePhase.LUTEAL
        }
    }

    /**
     * Returns the day number within the current cycle (1-indexed).
     * day 1 = first day of the most recent period.
     */
    fun currentCycleDay(
        mostRecentPeriodStart: java.time.LocalDate,
        today: java.time.LocalDate = java.time.LocalDate.now()
    ): Int {
        return mostRecentPeriodStart.until(today).days + 1
    }
}
