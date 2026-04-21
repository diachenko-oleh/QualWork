package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CourseInfoViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val intakeLogDao: IntakeLogDao
) : ViewModel() {

    var nextDoseTimes by mutableStateOf<Map<Long, String>>(emptyMap())
        private set

    init {
        viewModelScope.launch {
            repository.getAllWithSchedules().collect { courseList ->
                val result = mutableMapOf<Long, String>()
                courseList.forEach { medicationWithSchedules ->
                    medicationWithSchedules.schedules.forEach { schedule ->
                        val lastLog = intakeLogDao.getLastLog(schedule.id)
                        val nextDose = calculateNextDose(schedule, lastLog)
                        result[schedule.id] = formatDoseTime(nextDose)
                    }
                }
                nextDoseTimes = result
            }
        }
    }

    private fun calculateNextDose(schedule: Schedule, lastLog: IntakeLog?): Long {
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

        return if (lastLog == null) {
            if (now < firstDose) firstDose
            else {
                val dosesPassed = (now - firstDose) / intervalMs
                firstDose + dosesPassed * intervalMs
            }
        } else {
            lastLog.doseTime + intervalMs
        }
    }

    private fun formatDoseTime(timestamp: Long): String =
        SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
            .format(Date(timestamp))
}