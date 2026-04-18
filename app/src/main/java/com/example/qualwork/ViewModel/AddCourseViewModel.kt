package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.Model.Repository.MedicationRepository
import com.example.qualwork.Model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCourseViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val userPreferences: UserPreferences,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    // --- Крок 0: Користувач ---
    private var userId: String = ""

    init {
        viewModelScope.launch {
            userId = userPreferences.currentUserId.first() ?: ""
        }
    }

    // --- Крок 1: Препарат ---
    var medicationName by mutableStateOf("")
        private set
    var medicationForm by mutableStateOf(MedicationForm.TABLET)
        private set

    // --- Крок 2: Графік ---
    var intervalHours by mutableIntStateOf(8)
        private set
    var startTime by mutableStateOf("08:00")
        private set
    var dosage by mutableIntStateOf(1)
        private set

    // --- Крок 3: Тривалість ---
    var startDate by mutableLongStateOf(System.currentTimeMillis())
        private set
    var endDate by mutableStateOf<Long?>(null)
        private set

    // --- UI стан ---
    var isSaving by mutableStateOf(false)
        private set
    var savedSuccessfully by mutableStateOf(false)
        private set

    // --- Оновлення полів ---
    fun onNameChange(value: String) { medicationName = value }
    fun onFormChange(value: MedicationForm) { medicationForm = value }
    fun onIntervalChange(value: Int) { intervalHours = value }
    fun onStartTimeChange(value: String) { startTime = value }
    fun onDosageChange(value: Int) { dosage = value }
    fun onStartDateChange(value: Long) { startDate = value }
    fun onEndDateChange(value: Long?) { endDate = value }

    // --- Валідація ---
    fun isStep1Valid() = medicationName.isNotBlank()
    fun isStep2Valid() = dosage > 0
    fun isStep3Valid() = endDate == null || endDate!! > startDate

    fun saveCourse() {
        viewModelScope.launch {
            isSaving = true
            val scheduleId = repository.saveCourse(
                name = medicationName.trim(),
                form = medicationForm,
                startDate = startDate,
                endDate = endDate,
                startTime = startTime,
                intervalHours = intervalHours,
                dosage = dosage,
                userId = userId
            )
            notificationScheduler.scheduleNotifications(
                scheduleId = scheduleId,
                medicationName = medicationName.trim(),
                dosage = dosage,
                unit = medicationForm.unit,
                startTime = startTime,
                intervalHours = intervalHours,
                startDate = startDate,
                endDate = endDate
            )
            isSaving = false
            savedSuccessfully = true
        }
    }

    fun getScheduler(): NotificationScheduler = notificationScheduler
    val courses: StateFlow<List<MedicationWithSchedules>> =
        repository.getAllWithSchedules()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}