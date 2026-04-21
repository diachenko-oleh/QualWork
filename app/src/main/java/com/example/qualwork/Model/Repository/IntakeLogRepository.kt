package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.View.Treatment.formatDate
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.compareTo

class IntakeLogRepository @Inject constructor(
    private val intakeLogDao: IntakeLogDao
) {
    suspend fun logIntake(schedule: Schedule, taken: Boolean): Long {
        val currentDoseTime = calculateDoseTime(schedule)

        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = schedule.id,
                intakeTime = System.currentTimeMillis(),
                doseTime = currentDoseTime,
                intakeDate = formatDate(currentDoseTime),
                taken = taken
            )
        )
    }
    private fun formatDoseTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getBySchedule(scheduleId)

    private suspend fun calculateDoseTime(schedule: Schedule): Long {
        val now = System.currentTimeMillis()
        val intervalMs = schedule.intervalHours * 3600000L

        val (hours, minutes) = schedule.startTime.split(":").map { it.toInt() }
        val firstDose = Calendar.getInstance().apply {
            timeInMillis = schedule.startDate
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastLog = intakeLogDao.getLastLog(schedule.id)

        return if (lastLog == null) {
            // перший прийом
            if (now < firstDose) firstDose
            else {
                val dosesPassed = (now - firstDose) / intervalMs
                firstDose + dosesPassed * intervalMs
            }
        } else {
            // наступний прийом = lastLog.doseTime + інтервал
            val nextDose = lastLog.doseTime + intervalMs

            //android.util.Log.d("INTAKE_TEST", "lastLog.doseTime: ${formatDoseTime(lastLog.doseTime)}")
            //android.util.Log.d("INTAKE_TEST", "nextDose: ${formatDoseTime(nextDose)}")

            nextDose
        }
    }
}