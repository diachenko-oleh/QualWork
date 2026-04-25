package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class IntakeViewModel @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val notificationScheduler: NotificationScheduler,
    private val medicationRepository: MedicationRepository,
    private val intakeLogDao: IntakeLogDao,
    private val intakeTimeDao: IntakeTimeDao
) : ViewModel() {

    var medication by mutableStateOf<Medication?>(null)
        private set
    var schedule by mutableStateOf<Schedule?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var actionCompleted by mutableStateOf(false)
        private set

    fun takeMedication(time: LocalTime) {
        val currentSchedule = schedule ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
                    intakeTime = time,
                    taken = true
                )
                actionCompleted = true
            } finally {
                isLoading = false
            }
        }
    }

    fun skipMedication(time: LocalTime) {
        val currentSchedule = schedule ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
                    intakeTime = time,
                    taken = false
                )
                actionCompleted = true
            } finally {
                isLoading = false
            }
        }
    }


    var nextDoseTime by mutableStateOf<LocalTime?>(null)
        private set

    fun loadSchedule(scheduleId: Long) {
        viewModelScope.launch {
            val data = medicationRepository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == scheduleId } }
            medication = data?.medication
            schedule = data?.schedules?.firstOrNull { it.id == scheduleId }

            schedule?.let { s ->
                nextDoseTime = getNextDose(s.id)
            }
        }
    }

    private suspend fun getNextDose(scheduleId: Long): LocalTime? {

        val times = intakeTimeDao.getBySchedule(scheduleId)
        val logsToday = intakeLogDao.getTodayLogs(
            scheduleId,
            LocalDate.now().toString()
        )

        val now = LocalTime.now()

        val taken = logsToday.map { it.plannedDoseTime.toLocalTime() }.toSet()

        val sorted = times.map { LocalTime.parse(it.time) }

        val nextToday = sorted.firstOrNull {
            it > now && it !in taken
        }

        return nextToday ?: sorted.firstOrNull()
    }


}