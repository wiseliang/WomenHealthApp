package com.health.hormone.domain

import com.health.model.CervicalMucus
import com.health.model.CyclePhase
import com.health.model.DailySymptom
import com.health.model.HormoneLevel
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.*

class HormoneEstimatorTest {

    private val estimator = HormoneEstimator()

    @Test
    fun `follicular phase with egg-white mucus gives high estrogen`() {
        val symptoms = listOf(
            DailySymptom(
                date = LocalDate.now(),
                cervicalMucus = CervicalMucus.EGG_WHITE
            )
        )
        val result = estimator.assess(symptoms, CyclePhase.FOLLICULAR, cycleDay = 10)

        assertNotNull(result.estimatedEstrogen)
        assertTrue(result.estimatedEstrogen!! > 0.6, "Egg-white mucus in follicular should give high estrogen, got ${result.estimatedEstrogen}")
        assertEquals(HormoneLevel.HIGH, result.estrogenClass)
    }

    @Test
    fun `luteal phase with elevated BBT gives high progesterone`() {
        val symptoms = listOf(
            DailySymptom(
                date = LocalDate.now(),
                basalBodyTemp = 36.8
            )
        )
        val result = estimator.assess(symptoms, CyclePhase.LUTEAL, cycleDay = 20)

        assertNotNull(result.estimatedProgesterone)
        assertTrue(result.estimatedProgesterone!! > 0.7, "Luteal with high BBT should give high progesterone, got ${result.estimatedProgesterone}")
    }

    @Test
    fun `no symptoms still gives baseline assessment`() {
        val result = estimator.assess(emptyList(), CyclePhase.FOLLICULAR, cycleDay = 8)

        // Should have baselines
        assertNotNull(result.estimatedEstrogen)
        assertNotNull(result.estimatedProgesterone)
        // Confidence from phase-only
        assertEquals(0.30, result.confidenceScore)
    }

    @Test
    fun `more metrics increase confidence`() {
        val minimal = listOf(
            DailySymptom(date = LocalDate.now(), mood = 3)
        )
        val result1 = estimator.assess(minimal, CyclePhase.FOLLICULAR, cycleDay = 8)

        val rich = listOf(
            DailySymptom(
                date = LocalDate.now(),
                mood = 3,
                sleepQuality = 4,
                skinCondition = 3,
                cervicalMucus = CervicalMucus.WATERY,
                basalBodyTemp = 36.5
            )
        )
        val result2 = estimator.assess(rich, CyclePhase.FOLLICULAR, cycleDay = 8)

        assertTrue(result2.confidenceScore > result1.confidenceScore,
            "More metrics should increase confidence. Minimal: ${result1.confidenceScore}, Rich: ${result2.confidenceScore}")
    }

    @Test
    fun `menstrual phase gives low hormones`() {
        val symptoms = listOf(
            DailySymptom(
                date = LocalDate.now(),
                mood = 2,
                sleepQuality = 2
            )
        )
        val result = estimator.assess(symptoms, CyclePhase.MENSTRUAL, cycleDay = 2)

        assertTrue(result.estimatedEstrogen!! < 0.5, "Menstrual should have low estrogen")
        assertTrue(result.estimatedProgesterone!! < 0.5, "Menstrual should have low progesterone")
    }

    @Test
    fun `ovulatory phase gives peak estrogen`() {
        val symptoms = listOf(
            DailySymptom(
                date = LocalDate.now(),
                cervicalMucus = CervicalMucus.EGG_WHITE,
                mood = 5,
                skinCondition = 5
            )
        )
        val result = estimator.assess(symptoms, CyclePhase.OVULATORY, cycleDay = 14)

        assertNotNull(result.estimatedEstrogen)
        assertTrue(result.estimatedEstrogen!! >= 0.8, "Ovulatory with perfect symptoms should peak estrogen")
        assertEquals(HormoneLevel.HIGH, result.estrogenClass)
    }

    @Test
    fun `assessment method is recorded`() {
        val result = estimator.assess(emptyList(), CyclePhase.LUTEAL, cycleDay = 18)
        assertEquals("symptom_scoring_v1", result.assessmentMethod)
    }

    @Test
    fun `estrogen rises through follicular phase`() {
        val symptoms = emptyList<DailySymptom>()

        val early = estimator.assess(symptoms, CyclePhase.FOLLICULAR, cycleDay = 6)
        val late = estimator.assess(symptoms, CyclePhase.FOLLICULAR, cycleDay = 12)

        assertTrue(
            late.estimatedEstrogen!! > early.estimatedEstrogen!!,
            "Estrogen should rise from early to late follicular. Early: ${early.estimatedEstrogen}, Late: ${late.estimatedEstrogen}"
        )
    }

    @Test
    fun `dry mucus slightly reduces estrogen`() {
        val symptoms = listOf(
            DailySymptom(
                date = LocalDate.now(),
                cervicalMucus = CervicalMucus.DRY
            )
        )
        val noSymptoms = emptyList<DailySymptom>()

        val withDry = estimator.assess(symptoms, CyclePhase.FOLLICULAR, cycleDay = 10)
        val withoutDry = estimator.assess(noSymptoms, CyclePhase.FOLLICULAR, cycleDay = 10)

        assertTrue(
            withDry.estimatedEstrogen!! <= withoutDry.estimatedEstrogen!!,
            "Dry mucus should not increase estrogen"
        )
    }

    @Test
    fun `biphasic BBT pattern boosts confidence`() {
        val symptoms = (1..10).map { day ->
            DailySymptom(
                date = LocalDate.now().minusDays((10 - day).toLong()),
                basalBodyTemp = if (day <= 5) 36.3 else 36.7 // ~0.4C jump
            )
        }
        val result = estimator.assess(symptoms, CyclePhase.LUTEAL, cycleDay = 22)

        // Biphasic pattern detected should boost confidence
        assertTrue(result.confidenceScore >= 0.5,
            "Biphasic BBT pattern should boost confidence above base level")
    }
}
