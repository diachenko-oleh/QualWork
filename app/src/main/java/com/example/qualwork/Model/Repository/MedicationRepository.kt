package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.DAO.MedicationDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.Entity.IntakeTimeEntity
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val intakeTimeDao: IntakeTimeDao,
    private val scheduleDao: ScheduleDao,
    private val firestoreRepository: FirestoreRepository
) {
    suspend fun saveCourse(
        name: String,
        form: MedicationForm,
        startDate: Long,
        endDate: Long?,
        dosage: Int,
        userId: String,
        medAmount: Int?,
        intakeTimes: List<LocalTime>
    ): Long {
        val medicationId = medicationDao.insert(
            Medication(name = name, form = form)
        )
        val scheduleId = scheduleDao.insert(
            Schedule(
                medicationId = medicationId,
                startDate = startDate,
                endDate = endDate,
                dosage = dosage,
                userId = userId,
                medAmount = medAmount
            )
        )
        val intakeTimeEntities = intakeTimes.map {
            IntakeTimeEntity(scheduleId = scheduleId, time = it.toString())
        }
        val intakeTimeIds = intakeTimeDao.insertAll(intakeTimeEntities)

        val savedMedication = Medication(id = medicationId, name = name, form = form)
        val savedSchedule = Schedule(
            id = scheduleId,
            medicationId = medicationId,
            startDate = startDate,
            endDate = endDate,
            dosage = dosage,
            userId = userId,
            medAmount = medAmount
        )
        val savedIntakeTimes = intakeTimeEntities.mapIndexed { index, entity ->
            entity.copy(id = intakeTimeIds[index])
        }

        firestoreRepository.syncMedication(savedMedication, userId)
        firestoreRepository.syncSchedule(savedSchedule)
        firestoreRepository.syncIntakeTimes(scheduleId, userId, savedIntakeTimes)

        return scheduleId
    }

    suspend fun updateCourse(
        scheduleId: Long,
        name: String,
        form: MedicationForm,
        startDate: Long,
        endDate: Long?,
        dosage: Int,
        medAmount: Int?,
        intakeTimes: List<LocalTime>
    ) {
        val schedule = scheduleDao.getById(scheduleId) ?: return
        medicationDao.update(
            Medication(
                id = schedule.medicationId,
                name = name,
                form = form
            )
        )
        scheduleDao.update(
            schedule.copy(
                startDate = startDate,
                endDate = endDate,
                dosage = dosage,
                medAmount = medAmount
            )
        )
        intakeTimeDao.deleteBySchedule(scheduleId)
        val intakeTimeEntities = intakeTimes.map {
            IntakeTimeEntity(scheduleId = scheduleId, time = it.toString())
        }
        val intakeTimeIds = intakeTimeDao.insertAll(intakeTimeEntities)
        val savedIntakeTimes = intakeTimeEntities.mapIndexed { index, entity ->
            entity.copy(id = intakeTimeIds[index])
        }

        firestoreRepository.syncMedication(
            Medication(id = schedule.medicationId, name = name, form = form),
            schedule.userId
        )
        firestoreRepository.syncSchedule(schedule.copy(
            startDate = startDate,
            endDate = endDate,
            dosage = dosage,
            medAmount = medAmount
        ))
        firestoreRepository.syncIntakeTimes(scheduleId, schedule.userId, savedIntakeTimes)
    }

    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getById(id)
    }
    suspend fun deleteCourse(scheduleId: Long) {
        val schedule = scheduleDao.getById(scheduleId) ?: return
        medicationDao.delete(Medication(id = schedule.medicationId, name = "", form = MedicationForm.TABLET))

        firestoreRepository.deleteCourse(
            scheduleId = scheduleId,
            medicationId = schedule.medicationId,
            userId = schedule.userId
        )
    }
    suspend fun getIntakeTimes(scheduleId: Long): List<LocalTime> {
        return intakeTimeDao.getTimesForSchedule(scheduleId)
            .map { LocalTime.parse(it.time) }
    }

    fun getAllWithSchedules(): Flow<List<MedicationWithSchedules>> =
        medicationDao.getAllWithSchedules()

}