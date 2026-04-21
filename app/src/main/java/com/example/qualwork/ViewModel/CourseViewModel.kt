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
class CourseViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val userPreferences: UserPreferences,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {
    private var editingScheduleId: Long? = null

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val courseData = repository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == courseId } }
                ?: return@launch

            val medication = courseData.medication
            val schedule = courseData.schedules.first()

            editingScheduleId = schedule.id
            medicationName = medication.name
            medicationForm = medication.form
            intervalHours = schedule.intervalHours
            startTime = schedule.startTime
            dosage = schedule.dosage
            startDate = schedule.startDate
            endDate = schedule.endDate
        }
    }

    var deletedSuccessfully by mutableStateOf(false)
        private set

    fun deleteCourse(scheduleId: Long) {
        viewModelScope.launch {
            try {
                notificationScheduler.cancelNotifications(scheduleId)
                repository.deleteCourse(scheduleId)
                deletedSuccessfully = true
            } catch (e: Exception) {
                android.util.Log.e("AddCourse", "Помилка видалення: ${e.message}")
            }
        }
    }
    private var userId: String = ""
    init {
        viewModelScope.launch {
            userId = userPreferences.currentUserId.first() ?: ""
        }
    }

    //збереження препарату
    var medicationName by mutableStateOf("")
        private set
    var medicationForm by mutableStateOf(MedicationForm.TABLET)
        private set

    //збереження графіку/частоти
    var intervalHours by mutableIntStateOf(8)
        private set
    var startTime by mutableStateOf("08:00")
        private set
    var dosage by mutableIntStateOf(1)
        private set

    //збереження тривалості
    var startDate by mutableLongStateOf(System.currentTimeMillis())
        private set
    var endDate by mutableStateOf<Long?>(null)
        private set


    var isSaving by mutableStateOf(false)
        private set
    var savedSuccessfully by mutableStateOf(false)
        private set

    fun onNameChange(value: String) { medicationName = value }
    fun onFormChange(value: MedicationForm) { medicationForm = value }
    fun onIntervalChange(value: Int) { intervalHours = value }
    fun onStartTimeChange(value: String) { startTime = value }
    fun onDosageChange(value: Int) { dosage = value }
    fun onStartDateChange(value: Long) { startDate = value }
    fun onEndDateChange(value: Long?) { endDate = value }

    fun isStep1Valid() = medicationName.isNotBlank()
    fun isStep2Valid() = dosage > 0
    fun isStep3Valid() = endDate == null || endDate!! > startDate

    fun saveCourse() {
        viewModelScope.launch {
            isSaving = true
            try {
                val scheduleId = if (editingScheduleId == null) {
                    repository.saveCourse(
                        name = medicationName.trim(),
                        form = medicationForm,
                        startDate = startDate,
                        endDate = endDate,
                        startTime = startTime,
                        intervalHours = intervalHours,
                        dosage = dosage,
                        userId = userId
                    )
                } else {
                    repository.updateCourse(
                        scheduleId = editingScheduleId!!,
                        name = medicationName.trim(),
                        form = medicationForm,
                        startDate = startDate,
                        endDate = endDate,
                        startTime = startTime,
                        intervalHours = intervalHours,
                        dosage = dosage
                    )
                    editingScheduleId!!
                }

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
                savedSuccessfully = true
            } catch (e: Exception) {
                android.util.Log.e("AddCourse", "Помилка: ${e.message}")
            } finally {
                isSaving = false
            }
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