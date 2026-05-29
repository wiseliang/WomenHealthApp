package com.health.hormone.domain

import com.health.model.CervicalMucus
import com.health.model.CyclePhase
import com.health.model.DailySymptom
import com.health.model.HormoneAssessment
import com.health.model.HormoneLevel
import java.time.LocalDate

/**
 * Rule-based hormone estimation from daily symptom tracking.
 *
 * Produces a correlation-based estimate — NOT a medical measurement.
 * UI must always label results as "基于症状评估" with confidence scores.
 *
 * ### Estrogen Model (normalized 0.0-1.0)
 * Phase baseline:
 *   - MENSTRUAL: 0.25
 *   - FOLLICULAR early: 0.45 → rising to 0.85 peak
 *   - OVULATORY: 0.90
 *   - LUTEAL: 0.50 → declining to 0.20
 *
 * Modifiers:
 *   - Egg-white mucus: +0.30 (strong estrogen peak signal)
 *   - Watery mucus: +0.15
 *   - High mood (4-5): +0.05
 *   - Clear skin (4-5): +0.05
 *
 * ### Progesterone Model (normalized 0.0-1.0)
 * Phase baseline:
 *   - MENSTRUAL: 0.15
 *   - FOLLICULAR: 0.10
 *   - OVULATORY: 0.20
 *   - LUTEAL: 0.70
 *
 * Modifiers:
 *   - BBT elevated >= 0.3°C above baseline: +0.30 (confirms luteal)
 *   - Poor sleep (1-2): +0.10 (progesterone-related fatigue)
 *   - Low mood (1-2): +0.05
 *   - Poor skin (1-2): +0.05 (progesterone-related sebum)
 *
 * ### Confidence Score
 *   - 0.30 base (phase-only)
 *   - +0.10 per symptom metric provided (max +0.50)
 *   - +0.20 if BBT data shows biphasic pattern (3+ days elevated)
 *   - Capped at 1.0
 */
class HormoneEstimator {

    companion object {
        const val METHOD = "symptom_scoring_v1"
        const val BBT_ELEVATION_THRESHOLD = 0.3 // Celsius
    }

    fun assess(
        symptoms: List<DailySymptom>,
        cyclePhase: CyclePhase,
        cycleDay: Int,
        averageCycleLength: Int = 28
    ): HormoneAssessment {
        val today = LocalDate.now()
        val todaySymptom = symptoms.find { it.date == today }
        val recentSymptoms = symptoms.filter { it.date.isAfter(today.minusDays(7)) }

        // Compute baselines from phase and cycle day
        val estrogenBase = estrogenBaseline(cyclePhase, cycleDay, averageCycleLength)
        val progesteroneBase = progesteroneBaseline(cyclePhase)

        // Apply modifier scores
        var estrogenMod = 0.0
        var progesteroneMod = 0.0
        var metricsProvided = 0
        var bbtBiphasic = false

        todaySymptom?.let { s ->
            // Cervical mucus → estrogen
            s.cervicalMucus?.let { mucus ->
                metricsProvided++
                when (mucus) {
                    CervicalMucus.EGG_WHITE -> estrogenMod += 0.30
                    CervicalMucus.WATERY -> estrogenMod += 0.15
                    CervicalMucus.CREAMY -> estrogenMod += 0.05
                    CervicalMucus.STICKY -> { /* no modifier */ }
                    CervicalMucus.DRY -> estrogenMod -= 0.05
                }
            }

            // Mood
            s.mood?.let { mood ->
                metricsProvided++
                if (mood >= 4) estrogenMod += 0.05
                if (mood <= 2) progesteroneMod += 0.05
            }

            // Skin
            s.skinCondition?.let { skin ->
                metricsProvided++
                if (skin >= 4) estrogenMod += 0.05
                if (skin <= 2) progesteroneMod += 0.05
            }

            // Sleep
            s.sleepQuality?.let { sleep ->
                metricsProvided++
                if (sleep <= 2) progesteroneMod += 0.10
            }

            // BBT
            s.basalBodyTemp?.let { bbt ->
                metricsProvided++
                if (cyclePhase == CyclePhase.LUTEAL && bbt > 36.5) {
                    progesteroneMod += 0.30
                    // Check biphasic pattern
                    if (checkBiphasicBBT(recentSymptoms)) {
                        bbtBiphasic = true
                    }
                }
            }
        }

        // Assemble scores
        val estrogenScore = (estrogenBase + estrogenMod).coerceIn(0.0, 1.0)
        val progesteroneScore = (progesteroneBase + progesteroneMod).coerceIn(0.0, 1.0)

        // Confidence
        var confidence = 0.30 + (metricsProvided * 0.10)
        if (bbtBiphasic) confidence += 0.20
        confidence = confidence.coerceAtMost(1.0)

        return HormoneAssessment(
            date = today,
            estimatedEstrogen = estrogenScore,
            estimatedProgesterone = progesteroneScore,
            estrogenClass = classify(estrogenScore),
            progesteroneClass = classify(progesteroneScore),
            confidenceScore = confidence,
            assessmentMethod = METHOD
        )
    }

    private fun estrogenBaseline(phase: CyclePhase, cycleDay: Int, avgLength: Int): Double {
        return when (phase) {
            CyclePhase.MENSTRUAL -> 0.25
            CyclePhase.FOLLICULAR -> {
                val follicularDays = (avgLength / 2) - 5
                val follicularDay = (cycleDay - 6).coerceIn(1, follicularDays)
                // Linear ramp: 0.45 → 0.85 over follicular phase
                0.45 + (0.40 * follicularDay / follicularDays)
            }
            CyclePhase.OVULATORY -> 0.90
            CyclePhase.LUTEAL -> {
                // Decline from 0.50 → 0.20 over luteal phase
                val lutealDay = if (cycleDay > avgLength / 2 + 2) cycleDay - (avgLength / 2 + 2) else 1
                val lutealDays = avgLength - (avgLength / 2 + 2)
                (0.50 - 0.30 * lutealDay / lutealDays).coerceAtLeast(0.20)
            }
        }
    }

    private fun progesteroneBaseline(phase: CyclePhase): Double {
        return when (phase) {
            CyclePhase.MENSTRUAL -> 0.15
            CyclePhase.FOLLICULAR -> 0.10
            CyclePhase.OVULATORY -> 0.20
            CyclePhase.LUTEAL -> 0.70
        }
    }

    private fun checkBiphasicBBT(recentSymptoms: List<DailySymptom>): Boolean {
        val bbtData = recentSymptoms
            .filter { it.basalBodyTemp != null }
            .sortedBy { it.date }
            .takeLast(10)

        if (bbtData.size < 5) return false

        val firstHalf = bbtData.take(bbtData.size / 2)
        val secondHalf = bbtData.drop(bbtData.size / 2)
        val firstAvg = firstHalf.mapNotNull { it.basalBodyTemp }.average()
        val secondAvg = secondHalf.mapNotNull { it.basalBodyTemp }.average()

        return (secondAvg - firstAvg) >= BBT_ELEVATION_THRESHOLD
    }

    private fun classify(score: Double): HormoneLevel = when {
        score < 0.30 -> HormoneLevel.LOW
        score > 0.70 -> HormoneLevel.HIGH
        else -> HormoneLevel.NORMAL
    }
}
