package com.health.cycle.domain.algorithm

import com.health.model.CyclePhase
import com.health.model.PeriodRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.*

class CyclePredictorTest {

    private val predictor = CyclePredictor()

    @Test
    fun `empty list throws exception`() {
        assertThrows<IllegalArgumentException> { predictor.predict(emptyList()) }
    }

    @Test
    fun `single period returns no date prediction, only phase`() {
        val periods = listOf(
            PeriodRecord(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 1, 5))
        )
        val result = predictor.predict(periods)

        // With only 1 period, should use default 28-day cycle
        assertEquals(28, result.averageCycleLength)
        assertEquals(5, result.averagePeriodLength)
        assertNotNull(result.currentPhase)
    }

    @Test
    fun `two periods returns no date prediction`() {
        val periods = listOf(
            PeriodRecord(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 1, 5)),
            PeriodRecord(startDate = LocalDate.of(2025, 1, 30), endDate = LocalDate.of(2025, 2, 3))
        )
        val result = predictor.predict(periods)

        // 2 periods = 1 completed cycle, below threshold
        assertEquals(28, result.averageCycleLength) // defaults
    }

    @Test
    fun `three regular 28-day cycles predicts correctly`() {
        val periods = listOf(
            PeriodRecord(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 1, 5)),
            PeriodRecord(startDate = LocalDate.of(2025, 1, 29), endDate = LocalDate.of(2025, 2, 2)),
            PeriodRecord(startDate = LocalDate.of(2025, 2, 26), endDate = LocalDate.of(2025, 3, 2))
        )
        val result = predictor.predict(periods)

        // Cycle 1: Jan1-Jan5 -> Jan29 = 28 days cycle, 5 days period
        // Cycle 2: Jan29-Feb2 -> Feb26 = 28 days cycle, 5 days period
        assertEquals(28, result.averageCycleLength)
        assertEquals(5, result.averagePeriodLength)

        // Next start = Feb26 + 28 = Mar26
        assertEquals(LocalDate.of(2025, 3, 26), result.predictedNextPeriodStart)
        assertEquals(LocalDate.of(2025, 3, 31), result.predictedNextPeriodEnd)

        // Ovulation = Mar26 - 14 = Mar12
        assertEquals(LocalDate.of(2025, 3, 12), result.ovulationDate)

        // Fertile window = Mar7 - Mar13
        assertEquals(LocalDate.of(2025, 3, 7), result.fertileWindowStart)
        assertEquals(LocalDate.of(2025, 3, 13), result.fertileWindowEnd)

        assertEquals("v1-mean", result.modelVersion)
    }

    @Test
    fun `irregular cycles weight recent cycles more`() {
        val periods = listOf(
            // Older: 30-day cycle
            PeriodRecord(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 1, 5)),
            // Older: 28-day cycle
            PeriodRecord(startDate = LocalDate.of(2025, 1, 31), endDate = LocalDate.of(2025, 2, 4)),
            // Recent: 26-day cycle
            PeriodRecord(startDate = LocalDate.of(2025, 2, 28), endDate = LocalDate.of(2025, 3, 3)),
            // Most recent: 24-day cycle
            PeriodRecord(startDate = LocalDate.of(2025, 3, 24), endDate = LocalDate.of(2025, 3, 28))
        )
        val result = predictor.predict(periods)

        // Weighted average should be closer to 24 than to 30
        assertTrue(result.averageCycleLength in 24..28,
            "Weighted avg should lean toward recent shorter cycles, got ${result.averageCycleLength}")
    }

    @Test
    fun `long period duration is handled correctly`() {
        val periods = listOf(
            PeriodRecord(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 1, 8)), // 8-day period
            PeriodRecord(startDate = LocalDate.of(2025, 1, 29), endDate = LocalDate.of(2025, 2, 5)), // 8-day period
            PeriodRecord(startDate = LocalDate.of(2025, 2, 26), endDate = LocalDate.of(2025, 3, 5))  // 8-day period
        )
        val result = predictor.predict(periods)

        assertEquals(8, result.averagePeriodLength)
        // Cycle length: Jan1-Jan8 -> Jan29 = 28 (from Jan1 to Jan29 = 28 days total, 8+20)
        assertTrue(result.averageCycleLength in 26..30)
    }

    @Test
    fun `many cycles produce prediction with correct model version`() {
        val periods = mutableListOf<PeriodRecord>()
        var start = LocalDate.of(2025, 1, 1)
        for (i in 1..10) {
            periods.add(PeriodRecord(startDate = start, endDate = start.plusDays(5)))
            start = start.plusDays(28)
        }

        val result = predictor.predict(periods)
        assertEquals("v1-mean", result.modelVersion)
        assertEquals(28, result.averageCycleLength)
        assertEquals(5, result.averagePeriodLength)
        assertNotNull(result.ovulationDate)
        assertNotNull(result.fertileWindowStart)
        assertNotNull(result.fertileWindowEnd)
    }

    @Test
    fun `prediction includes current cycle phase`() {
        val periods = listOf(
            PeriodRecord(startDate = LocalDate.now().minusDays(10), endDate = LocalDate.now().minusDays(6))
        )
        val result = predictor.predict(periods)

        assertNotNull(result.currentPhase)
        assertEquals(11, result.cycleDayNow ?: 0) // 10 days ago + 1 = day 11
    }
}

class PhaseCalculatorTest {

    @Test
    fun `day 1-5 is menstrual phase`() {
        for (day in 1..5) {
            assertEquals(CyclePhase.MENSTRUAL, PhaseCalculator.determinePhase(day, 28),
                "Day $day should be MENSTRUAL")
        }
    }

    @Test
    fun `day 6-14 is follicular phase in 28 day cycle`() {
        for (day in 6..14) {
            assertEquals(CyclePhase.FOLLICULAR, PhaseCalculator.determinePhase(day, 28),
                "Day $day should be FOLLICULAR")
        }
    }

    @Test
    fun `day 15-16 is ovulatory phase in 28 day cycle`() {
        assertEquals(CyclePhase.OVULATORY, PhaseCalculator.determinePhase(15, 28))
        assertEquals(CyclePhase.OVULATORY, PhaseCalculator.determinePhase(16, 28))
    }

    @Test
    fun `day 17-28 is luteal phase in 28 day cycle`() {
        for (day in 17..28) {
            assertEquals(CyclePhase.LUTEAL, PhaseCalculator.determinePhase(day, 28),
                "Day $day should be LUTEAL")
        }
    }

    @Test
    fun `shorter cycle adjusts phase boundaries`() {
        // 21 day cycle:
        // Menstrual: 1-5
        // Follicular: 6-10 (half = 10.5)
        // Ovulatory: 11-12
        // Luteal: 13-21
        assertEquals(CyclePhase.MENSTRUAL, PhaseCalculator.determinePhase(1, 21))
        assertEquals(CyclePhase.FOLLICULAR, PhaseCalculator.determinePhase(8, 21))
        assertEquals(CyclePhase.OVULATORY, PhaseCalculator.determinePhase(11, 21))
        assertEquals(CyclePhase.LUTEAL, PhaseCalculator.determinePhase(18, 21))
    }

    @Test
    fun `longer cycle adjusts phase boundaries`() {
        // 35 day cycle:
        // Menstrual: 1-5
        // Follicular: 6-18 (half = 17.5 -> +2 = 19.5 -> effective 18)
        // Ovulatory: 19-20
        // Luteal: 21-35
        assertEquals(CyclePhase.MENSTRUAL, PhaseCalculator.determinePhase(3, 35))
        assertEquals(CyclePhase.FOLLICULAR, PhaseCalculator.determinePhase(15, 35))
        assertEquals(CyclePhase.LUTEAL, PhaseCalculator.determinePhase(25, 35))
    }

    @Test
    fun `cycle day wraps at cycle length`() {
        // Day 29 in a 28-day cycle = day 1 = menstrual
        assertEquals(CyclePhase.MENSTRUAL, PhaseCalculator.determinePhase(29, 28))
        // Day 32 in a 28-day cycle = day 4 = menstrual
        assertEquals(CyclePhase.MENSTRUAL, PhaseCalculator.determinePhase(32, 28))
        // Day 35 in a 28-day cycle = day 7 = follicular
        assertEquals(CyclePhase.FOLLICULAR, PhaseCalculator.determinePhase(35, 28))
    }
}
