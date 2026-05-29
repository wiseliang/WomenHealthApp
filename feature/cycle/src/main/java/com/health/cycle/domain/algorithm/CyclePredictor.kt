package com.health.cycle.domain.algorithm

import com.health.model.CyclePrediction
import com.health.model.CyclePhase
import com.health.model.PeriodRecord
import kotlin.math.pow

/**
 * Predicts next period start/end, ovulation, and fertile window
 * using exponentially weighted moving average of historical cycle data.
 *
 * Algorithm:
 * 1. Compute completed cycle lengths (end of one period to start of next)
 * 2. Weight recent cycles exponentially: w_i = 0.5^i (i=0 for most recent)
 * 3. Predicted start = last period start + weighted avg cycle length (rounded to nearest day)
 * 4. Predicted end = predicted start + weighted avg period length
 * 5. Ovulation = predicted next start - 14 days (fixed luteal phase assumption)
 * 6. Fertile window = ovulation -5 to ovulation +1
 *
 * Minimum 3 completed cycles required.
 * Below threshold, returns a prediction with cyclePhase only (no date estimates).
 */
class CyclePredictor {

    companion object {
        const val MIN_CYCLES_FOR_PREDICTION = 3
        const val MODEL_VERSION = "v1-mean"
        const val LUTEAL_PHASE_DAYS = 14
        const val FERTILE_WINDOW_BEFORE = 5
        const val FERTILE_WINDOW_AFTER = 1
        const val DECAY_FACTOR = 0.5
    }

    fun predict(periods: List<PeriodRecord>): CyclePrediction {
        require(periods.isNotEmpty()) { "At least one period record required" }

        val sorted = periods.sortedBy { it.startDate }
        val lastPeriod = sorted.last()

        // Compute completed cycle lengths: end[i] -> start[i+1]
        val cycleLengths = mutableListOf<Int>()
        val periodLengths = mutableListOf<Int>()

        for (i in 0 until sorted.size - 1) {
            val gapDays = sorted[i].endDate.until(sorted[i + 1].startDate).days
            cycleLengths.add(sorted[i].durationDays + gapDays)
            periodLengths.add(sorted[i].durationDays)
        }
        // Include last period's duration
        periodLengths.add(lastPeriod.durationDays)

        val averagePeriodLength = weightedAverage(periodLengths.map { it.toDouble() }).toInt()
        val cycleDayNow = lastPeriod.startDate.until(java.time.LocalDate.now()).days + 1
        val currentPhase = PhaseCalculator.determinePhase(
            cycleDay = cycleDayNow,
            averageCycleLength = if (cycleLengths.isNotEmpty()) cycleLengths.average().toInt() else 28
        )

        if (cycleLengths.size < MIN_CYCLES_FOR_PREDICTION) {
            return CyclePrediction(
                predictedNextPeriodStart = lastPeriod.startDate.plusDays(28L),
                predictedNextPeriodEnd = lastPeriod.startDate.plusDays(28L + averagePeriodLength),
                currentPhase = currentPhase,
                cycleDayNow = cycleDayNow,
                averageCycleLength = 28,
                averagePeriodLength = averagePeriodLength,
                modelVersion = MODEL_VERSION
            )
        }

        val averageCycleLength = weightedAverage(cycleLengths.map { it.toDouble() }).toInt()

        val predictedStart = lastPeriod.startDate.plusDays(averageCycleLength.toLong())
        val predictedEnd = predictedStart.plusDays(averagePeriodLength.toLong())
        val ovulationDate = predictedStart.minusDays(LUTEAL_PHASE_DAYS.toLong())
        val fertileStart = ovulationDate.minusDays(FERTILE_WINDOW_BEFORE.toLong())
        val fertileEnd = ovulationDate.plusDays(FERTILE_WINDOW_AFTER.toLong())

        return CyclePrediction(
            predictedNextPeriodStart = predictedStart,
            predictedNextPeriodEnd = predictedEnd,
            ovulationDate = ovulationDate,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            currentPhase = currentPhase,
            cycleDayNow = cycleDayNow,
            averageCycleLength = averageCycleLength,
            averagePeriodLength = averagePeriodLength,
            modelVersion = MODEL_VERSION
        )
    }

    private fun weightedAverage(values: List<Double>): Double {
        if (values.isEmpty()) return 28.0
        var weightSum = 0.0
        var weightedSum = 0.0
        for (i in values.indices) {
            val position = values.size - 1 - i // most recent = highest position
            val weight = DECAY_FACTOR.pow(position)
            weightedSum += values[i] * weight
            weightSum += weight
        }
        return weightedSum / weightSum
    }
}
