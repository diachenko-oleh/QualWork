package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CourseInfoViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val intakeLogDao: IntakeLogDao,
    private val intakeTimeDao: IntakeTimeDao
) : ViewModel() {

    var nextDoseTime by mutableStateOf<Map<Long, String>>(emptyMap())
        private set

    init {
        viewModelScope.launch {
            repository.getAllWithSchedules().collect { courseList ->
                val result = mutableMapOf<Long, String>()

                courseList.forEach { medicationWithSchedules ->
                    medicationWithSchedules.schedules.forEach { schedule ->

                        val nextDose = calculateNextDose(schedule.id)

                        result[schedule.id] = nextDose?.let { (time, isTomorrow) ->
                            val formattedTime = time.toString().substring(0, 5)

                            if (isTomorrow) {
                                "$formattedTime (завтра)"
                            } else {
                                formattedTime
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
        val nowDate = LocalDate.now()

        val takenTimes = logsToday
            .filter { it.taken }
            .map { it.plannedDoseTime.toLocalTime()  }
            .toSet()

        val sortedTimes = times
            .map { LocalTime.parse(it.time) }
            .sorted()

        val nextToday = sortedTimes.firstOrNull { time ->
            time.isAfter(nowTime) && time !in takenTimes
        }

        if (nextToday != null) {
            return nextToday to false
        }

        val tomorrowTime = sortedTimes.first()

        return tomorrowTime to true
    }
}