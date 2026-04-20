package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.View.Treatment.formatDate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IntakeLogRepository @Inject constructor(
    private val intakeLogDao: IntakeLogDao
) {
    suspend fun logIntake(scheduleId: Long, doseTime: Long, taken: Boolean): Long {
        return intakeLogDao.insert(
            IntakeLog(
                scheduleId = scheduleId,
                intakeTime = System.currentTimeMillis(),
                doseTime = doseTime,
                intakeDate = formatDate(System.currentTimeMillis()),
                taken = taken
            )
        )
    }

    fun getLogsForSchedule(scheduleId: Long): Flow<List<IntakeLog>> =
        intakeLogDao.getBySchedule(scheduleId)
}