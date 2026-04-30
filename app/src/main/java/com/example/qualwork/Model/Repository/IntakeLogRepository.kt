package com.example.qualwork.Model.Repository

import android.util.Log
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.ScheduleDao
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
    private val intakeLogDao: IntakeLogDao,
    private val scheduleDao: ScheduleDao,
    private val firestoreRepository: FirestoreRepository
) {
    suspend fun logIntake(
        schedule: Schedule,
        plannedTime: LocalTime,
        actualTime: LocalTime?,
        taken: Boolean): Pair<Long, Int?>
    {
        val today = LocalDate.now()
        val planned = today.atTime(plannedTime)
        val actual = actualTime?.let { today.atTime(it) }

        Log.d("INTAKE_DEBUG", "Saving log: plannedDoseTime=$planned, string=${planned}")

        val intakeLog = IntakeLog(
            scheduleId = schedule.id,
            plannedDoseTime = planned,
            actualDoseTime = actual,
            taken = taken
        )

        val id = intakeLogDao.insert(intakeLog)
        val savedLog = intakeLog.copy(id = id)

        firestoreRepository.syncIntakeLog(savedLog, schedule.userId)

        var updatedAmount: Int? = null

        if (taken) {
            schedule.medAmount?.let { current ->
                updatedAmount = (current - schedule.dosage).coerceAtLeast(0)
                updateMedAmount(schedule.id, updatedAmount)
            }
        }
        return id to updatedAmount
    }

    suspend fun logMissedIntake(
        scheduleId: Long,
        plannedTime: LocalTime
    ): Long {
        val planned = LocalDate.now().atTime(plannedTime)

        val intakeLog = IntakeLog(
            scheduleId = scheduleId,
            plannedDoseTime = planned,
            actualDoseTime = null,
            taken = false
        )
        val id = intakeLogDao.insert(intakeLog)
        val savedLog = intakeLog.copy(id = id)

        val schedule = scheduleDao.getById(scheduleId)
        schedule?.let {
            firestoreRepository.syncIntakeLog(savedLog, it.userId)
        }

        return id
    }
    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getByScheduleId(scheduleId)

    suspend fun updateMedAmount(scheduleId: Long, amount: Int) {
        scheduleDao.updateMedAmount(scheduleId, amount)
    }
    suspend fun checkIfTaken(scheduleId: Long, plannedDoseTime: LocalDateTime): Boolean {
        val dateString = plannedDoseTime.toLocalDate().toString()
        return intakeLogDao.isTaken(scheduleId, dateString) > 0
    }
}