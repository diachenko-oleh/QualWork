package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.Schedule
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.time.LocalDate
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
        taken: Boolean): Long {
        val nowTime = LocalTime.now()
        val date = LocalDate.now().toString()
        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = schedule.id,
                plannedDoseTime = intakeTime,
                actualDoseTime = if (taken) nowTime else null,
                intakeDate = date,
                taken = taken
            )
        )
    }
    private fun formatDoseTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getByScheduleId(scheduleId)

//    private suspend fun calculateDoseTime(schedule: Schedule): Long {
//        val now = System.currentTimeMillis()
//        val intervalMs = schedule.intervalHours * 3600000L
//
//        val (hours, minutes) = schedule.startTime.split(":").map { it.toInt() }
//        val firstDose = Calendar.getInstance().apply {
//            timeInMillis = schedule.startDate
//            set(Calendar.HOUR_OF_DAY, hours)
//            set(Calendar.MINUTE, minutes)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }.timeInMillis
//
//        val lastLog = intakeLogDao.getLastLog(schedule.id)
//
//        return if (lastLog == null) {
//            // перший прийом
//            if (now < firstDose) firstDose
//            else {
//                val dosesPassed = (now - firstDose) / intervalMs
//                firstDose + dosesPassed * intervalMs
//            }
//        } else {
//            // наступний прийом = lastLog.doseTime + інтервал
//            val nextDose = lastLog.doseTime + intervalMs
//
//            //android.util.Log.d("INTAKE_TEST", "lastLog.doseTime: ${formatDoseTime(lastLog.doseTime)}")
//            //android.util.Log.d("INTAKE_TEST", "nextDose: ${formatDoseTime(nextDose)}")
//
//            nextDose
//        }
//    }
}