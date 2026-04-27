package com.example.qualwork.Model.Entity

import java.time.LocalDate
import java.time.LocalTime

data class IntakeLogStat(
    val plannedTime: LocalTime,
    val actualTime: LocalTime?,
    val taken: Boolean
)

data class DayIntakeStat(
    val date: LocalDate,
    val intakes: List<IntakeLogStat>
)

enum class DayStatus {
    ALL_TAKEN,
    ALL_MISSED,
    PARTIAL,
    FUTURE
}

val DayIntakeStat.status: DayStatus
    get() {
        if (intakes.isEmpty()) return DayStatus.FUTURE
        val takenCount = intakes.count { it.taken }
        return when {
            takenCount == intakes.size -> DayStatus.ALL_TAKEN
            takenCount == 0 -> DayStatus.ALL_MISSED
            else -> DayStatus.PARTIAL
        }
    }