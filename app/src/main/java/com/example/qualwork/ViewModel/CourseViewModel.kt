package com.example.qualwork.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.Entity.DayIntakeStat
import com.example.qualwork.Model.Entity.IntakeLogStat
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import com.example.qualwork.Model.Repository.UserRepository
import com.example.qualwork.Model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val medRepository: MedicationRepository,
    private val intakeRepository: IntakeLogRepository,
    private val intakeLogDao: IntakeLogDao,
    private val intakeTimeDao: IntakeTimeDao,
    private val scheduleDao: ScheduleDao,
    private val notificationScheduler: NotificationScheduler,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository

) : ViewModel()
{
    private var editingScheduleId: Long? = null
    val medAmounts: StateFlow<Map<Long, Int?>> =  medRepository.getAllWithSchedules()
            .map { courses ->
                courses.flatMap { it.schedules }
                    .associate { it.id to it.medAmount }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    var courseIntakeTimes by mutableStateOf<List<String>>(emptyList())
        private set
    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val courseData = medRepository.getAllWithSchedules().first()
                .find { it.schedules.any { s -> s.id == courseId } }
                ?: return@launch

            val medication = courseData.medication
            val schedule = courseData.schedules.first()


            editingScheduleId = schedule.id
            medicationName = medication.name
            medicationForm = medication.form
            dosage = schedule.dosage
            startDate = schedule.startDate
            endDate = schedule.endDate
            medAmount = schedule.medAmount

            val times = medRepository.getIntakeTimes(schedule.id)

            intakeTimes = times.map { formatTime(it) }
            courseIntakeTimes = times.map { it.toString().substring(0, 5) }
            frequencyPerDay = intakeTimes.size
        }
    }

    var deletedSuccessfully by mutableStateOf(false)
        private set

    fun deleteCourse(scheduleId: Long) {
        viewModelScope.launch {
            try {
                notificationScheduler.cancelNotifications(scheduleId)
                medRepository.deleteCourse(scheduleId)
                deletedSuccessfully = true
            } catch (e: Exception) {
                Log.e("AddCourse", "Помилка видалення: ${e.message}")
            }
        }
    }

    private var userId: String = ""
    private var userName: String = ""
    init {
        viewModelScope.launch {
            userId = userPreferences.currentUserId.first() ?: ""
            userName = userRepository.getById(userId)?.name ?: ""
        }
    }

    //збереження препарату
    var medicationName by mutableStateOf("")
        private set
    var medicationForm by mutableStateOf(MedicationForm.TABLET)
        private set

    //збереження графіку/частоти
    var frequencyPerDay by mutableIntStateOf(1)
        private set
    var intakeTimes by mutableStateOf(List(1) { "08:00" })
        private set
    var dosage by mutableIntStateOf(1)
        private set

    //збереження тривалості
    var startDate by mutableLongStateOf(System.currentTimeMillis())
        private set
    var endDate by mutableStateOf<Long?>(
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 6)
        }.timeInMillis
    )
        private set
    var duration by mutableStateOf<CourseDuration?>(CourseDuration.WEEK_1)
        private set
    var isCustomDuration by mutableStateOf(false)
        private set
    var medAmount by mutableStateOf<Int?>(null)
        private set


    var isSaving by mutableStateOf(false)
        private set
    var savedSuccessfully by mutableStateOf(false)
        private set

    fun onNameChange(value: String) { medicationName = value }
    fun onFormChange(value: MedicationForm) { medicationForm = value }
    fun onFrequencyChange(value: Int) {
        frequencyPerDay = value

        intakeTimes = List(value) { index ->
            intakeTimes.getOrNull(index) ?: "08:00"
        }
    }
    fun onIntakeTimeChange(index: Int, time: String) {
        intakeTimes = intakeTimes.toMutableList().also {
            it[index] = time
        }
    }
    fun onStartDateChange(date: Long) {
        startDate = date
        recalculateEndDate()
    }
    fun onDurationSelected(newDuration: CourseDuration) {
        duration = newDuration
        recalculateEndDate()
    }
    fun onEndDateChange(date: Long) {
        endDate = date
    }
    fun onCustomDurationChange(value: Boolean) {
        isCustomDuration = value
        if (!value) {
            duration = CourseDuration.WEEK_1
            recalculateEndDate()
        }
    }
    fun onMedAmountChange(value: Int?) {
        medAmount = value
    }
    fun refillMedAmount(amountToAdd: Int,schedule: Schedule) {
        if (amountToAdd <= 0) return

        val current = medAmount ?: 0
        val newAmount = current + amountToAdd

        medAmount = newAmount

        viewModelScope.launch {
            schedule.let {
                scheduleDao.updateMedAmount(it.id, newAmount)
            }
        }
    }
    fun onDosageChange(value: Int) {
        dosage = value
    }

    private fun recalculateEndDate() {
        val d = duration ?: return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDate
        }

        when (d) {
            CourseDuration.WEEK_1 -> calendar.add(Calendar.DAY_OF_YEAR, 6)
            CourseDuration.WEEK_2 -> calendar.add(Calendar.DAY_OF_YEAR, 13)
            CourseDuration.WEEK_3 -> calendar.add(Calendar.DAY_OF_YEAR, 20)
            CourseDuration.MONTH_1 -> calendar.add(Calendar.DAY_OF_YEAR, 27)
            CourseDuration.MONTH_2 -> calendar.add(Calendar.DAY_OF_YEAR, 55)
            CourseDuration.MONTH_3 -> calendar.add(Calendar.DAY_OF_YEAR, 83)
        }

        endDate = calendar.timeInMillis
    }

    fun isStep1Valid() = medicationName.isNotBlank()
    fun isStep2Valid() = dosage > 0
    fun isStep3Valid() = endDate == null || endDate!! > startDate


    fun saveCourse() {
        viewModelScope.launch {
            isSaving = true
            try {
                val parsedTimes = intakeTimes.map { LocalTime.parse(it) }
                val scheduleId = if (editingScheduleId == null) {
                    medRepository.saveCourse(
                        name = medicationName.trim(),
                        form = medicationForm,
                        startDate = startDate,
                        endDate = endDate,
                        dosage = dosage,
                        userId = userId,
                        medAmount = medAmount,
                        intakeTimes = parsedTimes
                    )
                } else {
                    medRepository.updateCourse(
                        scheduleId = editingScheduleId!!,
                        name = medicationName.trim(),
                        form = medicationForm,
                        startDate = startDate,
                        endDate = endDate,
                        dosage = dosage,
                        medAmount = medAmount,
                        intakeTimes = parsedTimes
                    )

                    editingScheduleId!!
                }
                notificationScheduler.scheduleNotifications(
                    scheduleId = scheduleId,
                    medicationName = medicationName.trim(),
                    dosage = dosage,
                    unit = medicationForm.unit,
                    startDate = startDate,
                    endDate = endDate,
                    intakeTimes = parsedTimes,
                    userId = userId,
                    userName = userName
                )
                savedSuccessfully = true
            } catch (e: Exception) {
                    Log.e("AddCourse", "Помилка: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    val courses: StateFlow<List<MedicationWithSchedules>> = medRepository.getAllWithSchedules()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun getDetailedStats(scheduleId: Long): Flow<List<DayIntakeStat>> {
        return intakeRepository.getLogsForSchedule(scheduleId)
            .map { logs ->
                logs
                    .groupBy { it.plannedDoseTime.toLocalDate() }
                    .map { (date, items) ->
                        DayIntakeStat(
                            date = date,
                            intakes = items.map { log ->
                                IntakeLogStat(
                                    plannedTime = log.plannedDoseTime.toLocalTime(),
                                    actualTime = log.actualDoseTime?.toLocalTime(),
                                    taken = log.taken
                                )
                            }.sortedBy { it.plannedTime }
                        )
                    }
                    .sortedByDescending { it.date }
            }
    }

    var activeIntakeTime by mutableStateOf<LocalTime?>(null)
        private set
    fun startWatchingActiveIntake(scheduleId: Long) {
        viewModelScope.launch {
            while (true) {
                activeIntakeTime = findActiveIntakeTime(scheduleId)
                delay(60_000)
            }
        }
    }

    private suspend fun findActiveIntakeTime(scheduleId: Long): LocalTime? {
        val times = intakeTimeDao.getBySchedule(scheduleId)
        val now = LocalTime.now()
        val today = LocalDate.now().toString()
        val logsToday = intakeLogDao.getTodayLogs(scheduleId, today)
        val loggedTimes = logsToday.map { it.plannedDoseTime.toLocalTime() }.toSet()

        Log.d("INTAKE_DEBUG", "refreshActiveIntake: scheduleId=$scheduleId")
        Log.d("INTAKE_DEBUG", "  logsToday=${logsToday.map { "${it.plannedDoseTime} taken=${it.taken}" }}")
        Log.d("INTAKE_DEBUG", "  loggedTimes=$loggedTimes")
        Log.d("INTAKE_DEBUG", "  now=$now")

        return times
            .map { LocalTime.parse(it.time) }
            .firstOrNull { time ->
                val diff = Duration.between(time, now).toMinutes()
                Log.d("INTAKE_DEBUG", "  time=$time, diff=$diff")
                diff in 0..9 && time !in loggedTimes        //10хвилин очікування
            }
    }


    fun refreshActiveIntake(scheduleId: Long) {
        viewModelScope.launch {
            activeIntakeTime = findActiveIntakeTime(scheduleId)
        }
    }

    enum class CourseDuration(val label: String) {
        WEEK_1("1 тиждень"),
        WEEK_2("2 тижні"),
        WEEK_3("3 тижні"),
        MONTH_1("1 місяць"),
        MONTH_2("2 місяці"),
        MONTH_3("3 місяці")
    }

    fun getCalendarStats(
        scheduleId: Long,
        startDateMillis: Long,
        endDateMillis: Long?
    ): Flow<List<List<DayIntakeStat>>> {
        return getDetailedStats(scheduleId).map { stats ->
            val start = Instant.ofEpochMilli(startDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val end = endDateMillis?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            } ?: LocalDate.now()

            Log.d("CALENDAR_DEBUG", "startDate=$start, endDate=$end")

            val statsMap = stats.associateBy { it.date }

            generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .map { date ->
                    statsMap[date] ?: DayIntakeStat(date = date, intakes = emptyList())
                }
                .toList()
                .chunked(7)
        }
    }
}