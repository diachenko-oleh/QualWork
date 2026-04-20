package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntakeViewModel @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val notificationScheduler: NotificationScheduler,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    var medication by mutableStateOf<Medication?>(null)
        private set
    var schedule by mutableStateOf<Schedule?>(null)
        private set

    fun loadSchedule(scheduleId: Long) {
        viewModelScope.launch {
            val data = medicationRepository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == scheduleId } }
            medication = data?.medication
            schedule = data?.schedules?.firstOrNull { it.id == scheduleId }
        }
    }
    var isLoading by mutableStateOf(false)
        private set
    var actionCompleted by mutableStateOf(false)
        private set

    fun takeMedication(scheduleId: Long, doseTime: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                android.util.Log.d("INTAKE_TEST", "takeMedication: scheduleId=$scheduleId, doseTime=$doseTime")
                intakeLogRepository.logIntake(
                    scheduleId = scheduleId,
                    doseTime = doseTime,
                    taken = true
                )
                android.util.Log.d("INTAKE_TEST", "logIntake успішно")
                actionCompleted = true
            } catch (e: Exception) {
                android.util.Log.e("INTAKE_TEST", "Помилка: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun skipMedication(scheduleId: Long, doseTime: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    scheduleId = scheduleId,
                    doseTime = doseTime,
                    taken = false
                )
                actionCompleted = true
            } catch (e: Exception) {
                android.util.Log.e("IntakeViewModel", "Помилка: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun snoozeMedication(scheduleId: Long, doseTime: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    scheduleId = scheduleId,
                    doseTime = doseTime,
                    taken = false
                )
                notificationScheduler.scheduleDelayed(
                    delayMinutes = 30,
                    medicationName = medication?.name ?: "",
                    dosage = schedule?.dosage ?: 1,
                    unit = medication?.form?.unit ?: ""
                )
                actionCompleted = true
            } catch (e: Exception) {
                android.util.Log.e("IntakeViewModel", "Помилка: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}