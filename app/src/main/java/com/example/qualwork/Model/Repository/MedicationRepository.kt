package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.MedicationDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val scheduleDao: ScheduleDao
) {
    suspend fun saveCourse(
        name: String,
        form: MedicationForm,
        startDate: Long,
        endDate: Long?,
        startTime: String,
        intervalHours: Int,
        dosage: Int,
        userId: String
    ): Long {
        val medicationId = medicationDao.insert(
            Medication(name = name, form = form)
        )
        return scheduleDao.insert(
            Schedule(
                medicationId = medicationId,
                startDate = startDate,
                endDate = endDate,
                startTime = startTime,
                intervalHours = intervalHours,
                dosage = dosage,
                userId = userId
            )
        )
    }

    fun getAllWithSchedules(): Flow<List<MedicationWithSchedules>> =
        medicationDao.getAllWithSchedules()

    fun getActiveSchedules(today: Long): Flow<List<Schedule>> =
        scheduleDao.getActiveSchedules(today)

    suspend fun deleteCourse(medication: Medication) =
        medicationDao.delete(medication)
}