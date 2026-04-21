package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class IntakeViewModel @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val notificationScheduler: NotificationScheduler,
    private val medicationRepository: MedicationRepository,
    private val intakeLogDao: IntakeLogDao
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
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
                    taken = true
                )
                actionCompleted = true
            } catch (e: Exception) {
                android.util.Log.e("IntakeViewModel", "Помилка: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun skipMedication() {
        val currentSchedule = schedule ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                intakeLogRepository.logIntake(
                    schedule = currentSchedule,
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

    fun snoozeMedication() {
        val currentSchedule = schedule ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                notificationScheduler.scheduleDelayed(
                    delayMinutes = 30,
                    medicationName = medication?.name ?: "",
                    dosage = currentSchedule.dosage,
                    unit = medication?.form?.unit ?: "",
                    scheduleId = currentSchedule.id
                )
                actionCompleted = true
            } catch (e: Exception) {
                android.util.Log.e("IntakeViewModel", "Помилка: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    var nextDoseTime by mutableLongStateOf(0L)
        private set

    fun loadSchedule(scheduleId: Long) {
        viewModelScope.launch {
            val data = medicationRepository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == scheduleId } }
            medication = data?.medication
            schedule = data?.schedules?.firstOrNull { it.id == scheduleId }

            schedule?.let {
                nextDoseTime = calculateDoseTime(it)
                android.util.Log.d("INTAKE_TEST", "nextDoseTime: $nextDoseTime = ${formatDoseTime(nextDoseTime)}")
            }
        }
    }

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

        // Отримуємо останній записаний прийом для цього розкладу
        val lastLog = intakeLogDao.getLastLog(schedule.id)

        val nextDoseTime = if (lastLog == null) {
            // Перший прийом — повертаємо firstDose або поточну дозу
            if (now < firstDose) firstDose
            else {
                val dosesPassed = (now - firstDose) / intervalMs
                firstDose + dosesPassed * intervalMs
            }
        } else {
            // Наступний прийом після останнього записаного
            lastLog.doseTime + intervalMs
        }

        android.util.Log.d("INTAKE_TEST", "lastLog doseTime: ${lastLog?.doseTime?.let { formatDoseTime(it) } ?: "null"}")
        android.util.Log.d("INTAKE_TEST", "nextDoseTime: ${formatDoseTime(nextDoseTime)}")

        return nextDoseTime
    }

     private fun formatDoseTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}