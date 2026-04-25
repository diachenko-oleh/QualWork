package com.example.qualwork.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import com.example.qualwork.Model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CourseInfoViewModel @Inject constructor(
    private val medRepository: MedicationRepository,
    private val intakeRepository: IntakeLogRepository,
    private val intakeLogDao: IntakeLogDao,
    private val intakeTimeDao: IntakeTimeDao,
    private val notificationScheduler: NotificationScheduler,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var nextDoseTime by mutableStateOf<Map<Long, String>>(emptyMap())
        private set

    init {
        viewModelScope.launch {
            combine(
                medRepository.getAllWithSchedules(),
                intakeLogDao.observeAll()
            ){courses, _ -> courses}.collect {
            courseList ->
            val result = mutableMapOf<Long, String>()

            courseList.forEach { medicationWithSchedules ->
                medicationWithSchedules.schedules.forEach { schedule ->

                    val nextDose = calculateNextDose(schedule.id)

                    result[schedule.id] = nextDose?.let { (time, isTomorrow) ->
                        val formattedTime = time.toString().substring(0, 5)

                        if (isTomorrow) {
                            "$formattedTime (завтра)"
                        } else {
                            "$formattedTime (сьогодні)"
                        }
                    } ?: "—"
                }
            }
            nextDoseTime = result
            }
        }
    }

    private suspend fun calculateNextDose(scheduleId: Long): Pair<LocalTime, Boolean>? {
        val times = intakeTimeDao.getBySchedule(scheduleId)
        if (times.isEmpty()) return null

        val today = LocalDate.now().toString()
        val logsToday = intakeLogDao.getTodayLogs(scheduleId, today)
        val nowTime = LocalTime.now()

        Log.d("INTAKE_DEBUG", "calculateNextDose scheduleId=$scheduleId")
        Log.d("INTAKE_DEBUG", "  today=$today")
        Log.d("INTAKE_DEBUG", "  logsToday=${logsToday.map { "${it.plannedDoseTime} taken=${it.taken}" }}")
        Log.d("INTAKE_DEBUG", "  nowTime=$nowTime")


        val takenTimes = logsToday
            .filter { it.taken }
            .map { it.plannedDoseTime.toLocalTime() }
            .toSet()

        Log.d("INTAKE_DEBUG", "  takenTimes=$takenTimes")

        val sortedTimes = times.map { LocalTime.parse(it.time) }.sorted()
        val nextToday = sortedTimes.firstOrNull { it.isAfter(nowTime) && it !in takenTimes }

        Log.d("INTAKE_DEBUG", "  nextToday=$nextToday, fallback=${sortedTimes.first()}")

        if (nextToday != null) return nextToday to false
        return sortedTimes.first() to true


    }
}