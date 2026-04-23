package com.example.qualwork.Model.Notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.View.Treatment.formatDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MissedWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val intakeLogDao: IntakeLogDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong("scheduleId", -1)
        val doseTime = inputData.getLong("doseTime", -1)

        if (scheduleId == -1L || doseTime == -1L) return Result.failure()

        val existing = intakeLogDao.getByScheduleAndDoseTime(scheduleId, doseTime)

        if (existing == null) {
            intakeLogDao.insert(
                IntakeLog(
                    scheduleId = scheduleId,
                    intakeTime = System.currentTimeMillis(),
                    doseTime = doseTime,
                    intakeDate = formatDate(doseTime),
                    taken = false
                )
            )
        }

        return Result.success()
    }
}