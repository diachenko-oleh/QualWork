package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.Schedule
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class IntakeLogRepository @Inject constructor(
    private val intakeLogDao: IntakeLogDao
) {
    suspend fun logIntake(
        schedule: Schedule,
        intakeTime: LocalTime,
        taken: Boolean): Long
    {
        val now = LocalDateTime.now()
        val planned = LocalDate.now()
            .atTime(intakeTime)
        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = schedule.id,
                plannedDoseTime = planned,
                actualDoseTime = if (taken) now else null,
                taken = taken
            )
        )
    }

    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getByScheduleId(scheduleId)
}