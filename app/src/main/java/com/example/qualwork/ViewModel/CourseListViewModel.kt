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
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.Model.Relation.PatientCourseGroup
import com.example.qualwork.Model.Repository.FirestoreRepository
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import com.example.qualwork.Model.Repository.UserRepository
import com.example.qualwork.Model.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val medRepository: MedicationRepository,
    private val intakeRepository: IntakeLogRepository,
    private val intakeLogDao: IntakeLogDao,
    private val intakeTimeDao: IntakeTimeDao,
    private val notificationScheduler: NotificationScheduler,
    private val userPreferences: UserPreferences,
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository
) : ViewModel()
{
    var nextDoseTime by mutableStateOf<Map<Long, String>>(emptyMap())
        private set
    var patientCourseGroups by mutableStateOf<List<PatientCourseGroup>>(emptyList())
        private set
    var isLoadingPatientCourses by mutableStateOf(false)
        private set
    private val _userId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _userId.value = userPreferences.currentUserId.first() ?: ""

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
        Log.d("NEXT_DOSE_DEBUG", "calculateNextDose called for scheduleId=$scheduleId")
        val times = intakeTimeDao.getBySchedule(scheduleId)
        if (times.isEmpty()) return null

        val today = LocalDate.now().toString()
        val logsToday = intakeLogDao.getTodayLogs(scheduleId, today)
        val nowTime = LocalTime.now()
        Log.d("NEXT_DOSE_DEBUG", "calculateNextDose called for scheduleId=$scheduleId")

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
        val nextToday = sortedTimes.firstOrNull     {!it.isBefore(nowTime.minusMinutes(1)) && it !in takenTimes}
            // { it.isAfter(nowTime) && it !in takenTimes }

            Log.d("INTAKE_DEBUG", "  nextToday=$nextToday, fallback=${sortedTimes.first()}")

        if (nextToday != null) return nextToday to false
        return sortedTimes.first() to true


    }

    fun loadPatientCourses() {
        viewModelScope.launch {
            val currentUserId = _userId.value.ifEmpty {
                userPreferences.currentUserId.first() ?: return@launch
            }
            isLoadingPatientCourses = true
            try {
                val patients = userRepository.getPatients(currentUserId)

                patientCourseGroups = patients.map { patient ->
                    val (medications, schedules) = firestoreRepository.getPatientFullData(patient.id)
                    val intakeTimes = firestoreRepository.getPatientIntakeTimes(patient.id)
                    val intakeLogs = firestoreRepository.getPatientIntakeLogs(patient.id)

                    val patientNextDoseTimes = schedules.associate { schedule ->
                        val scheduleTimes = intakeTimes
                            .filter { it.scheduleId == schedule.id }
                            .map { LocalTime.parse(it.time) }
                            .sorted()

                        val today = LocalDate.now().toString()
                        val nowTime = LocalTime.now()

                        val takenTimes = intakeLogs
                            .filter { log ->
                                log.scheduleId == schedule.id &&
                                        log.taken &&
                                        log.plannedDoseTime.toLocalDate().toString() == today
                            }
                            .map { it.plannedDoseTime.toLocalTime() }
                            .toSet()
                        val nextToday = scheduleTimes.firstOrNull {
                            !it.isBefore(nowTime.minusMinutes(1)) && it !in takenTimes
                        }

                        val result = when {
                            scheduleTimes.isEmpty() -> "—"
                            nextToday != null -> "${nextToday.toString().substring(0, 5)} (сьогодні)"
                            else -> "${scheduleTimes.first().toString().substring(0, 5)} (завтра)"
                        }

                        schedule.id to result
                    }
                    val courses = medications.map { medication ->
                        MedicationWithSchedules(
                            medication = medication,
                            schedules = schedules.filter { it.medicationId == medication.id }
                        )
                    }.filter { it.schedules.isNotEmpty() }

                    PatientCourseGroup(
                        patientName = patient.name,
                        patientId = patient.id,
                        courses = courses,
                        nextDoseTimes = patientNextDoseTimes
                    )
                }
            } catch (e: Exception) {
                Log.e("CourseInfoVM", "Помилка: $e")
            } finally {
                isLoadingPatientCourses = false
            }
        }
    }

    private var missedNotificationListener: ListenerRegistration? = null

    fun startObservingMissedNotifications(
        patientIds: List<String>,
        onMissed: (String, String, String) -> Unit
    ) {
        missedNotificationListener?.remove() // зупинити попередній слухач

        if (patientIds.isEmpty()) return

        missedNotificationListener = Firebase.firestore
            .collection("missed_notifications")
            .whereIn("patientId", patientIds)
            .whereEqualTo("seen", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .forEach { change ->
                        val data = change.document.data
                        val patientName = data["patientName"] as? String ?: ""
                        val medicationName = data["medicationName"] as? String ?: ""
                        val time = data["time"] as? String ?: ""

                        onMissed(patientName, medicationName, time)

                        // Позначити як переглянуте
                        firestoreRepository.markNotificationSeen(change.document.id)
                    }
            }
    }

    override fun onCleared() {
        super.onCleared()
        missedNotificationListener?.remove()
    }
}