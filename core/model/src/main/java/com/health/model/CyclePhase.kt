package com.health.model

import kotlinx.serialization.Serializable

@Serializable
enum class CyclePhase(val displayName: String, val dayRangeDescription: String) {
    MENSTRUAL("月经期", "周期第1-5天"),
    FOLLICULAR("卵泡期", "周期第6-13天"),
    OVULATORY("排卵期", "周期第14天前后"),
    LUTEAL("黄体期", "周期第15-28天");

    companion object {
        fun fromCycleDay(dayOfCycle: Int, averageCycleLength: Int = 28): CyclePhase {
            return when {
                dayOfCycle <= 5 -> MENSTRUAL
                dayOfCycle <= 13 -> FOLLICULAR
                dayOfCycle <= 15 -> OVULATORY
                dayOfCycle <= averageCycleLength -> LUTEAL
                else -> MENSTRUAL
            }
        }
    }
}
