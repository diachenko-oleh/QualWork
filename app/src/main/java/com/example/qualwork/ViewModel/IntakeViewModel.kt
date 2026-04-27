package com.example.qualwork.ViewModel

import android.util.Log
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

    fun takeMedication() {
        val currentSchedule = schedule ?: return
        val plannedTime = nextDoseTime ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
                    plannedTime = plannedTime,
                    actualTime = LocalTime.now(),
                    taken = true
                )
                actionCompleted = true
            } finally {
                isLoading = false
            }
        }
    }

    fun skipMedication() {
        val currentSchedule = schedule ?: return
        val plannedTime = nextDoseTime ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
                    plannedTime = plannedTime,
                    actualTime = null,
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

    fun loadSchedule(scheduleId: Long, doseTime: LocalTime) {
        viewModelScope.launch {
            val data = medicationRepository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == scheduleId } }
            medication = data?.medication
            schedule = data?.schedules?.firstOrNull { it.id == scheduleId }
            nextDoseTime = doseTime
        }
    }



}