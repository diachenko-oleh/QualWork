package com.example.qualwork.Model.Repository

import android.util.Log
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
        plannedTime: LocalTime,
        actualTime: LocalTime?,
        taken: Boolean): Long
    {
        val today = LocalDate.now()
        val planned = today.atTime(plannedTime)
        val actual = actualTime?.let { today.atTime(it) }

        Log.d("INTAKE_DEBUG", "Saving log: plannedDoseTime=$planned, string=${planned.toString()}")

        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = schedule.id,
                plannedDoseTime = planned,
                actualDoseTime = actual,
                taken = taken
            )
        )
    }

    suspend fun logMissedIntake(
        scheduleId: Long,
        plannedTime: LocalTime
    ): Long {
        val planned = LocalDate.now().atTime(plannedTime)

        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = scheduleId,
                plannedDoseTime = planned,
                actualDoseTime = null,
                taken = false
            )
        )
    }
    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getByScheduleId(scheduleId)

    suspend fun checkIfTaken(scheduleId: Long, plannedDoseTime: LocalDateTime): Boolean {
        val dateString = plannedDoseTime.toLocalDate().toString() // "yyyy-MM-dd"
        return intakeLogDao.isTaken(scheduleId, dateString) > 0
    }
}