package com.example.qualwork.Model.Notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotifications(
        scheduleId: Long,
        medicationName: String,
        dosage: Int,
        unit: String,
        startTime: String,
        intervalHours: Int,
        startDate: Long,
        endDate: Long?
    ) {
        cancelNotifications(scheduleId)

        val inputData = workDataOf(
            NotificationWorker.KEY_MEDICATION_NAME to medicationName,
            NotificationWorker.KEY_DOSAGE to dosage,
            NotificationWorker.KEY_UNIT to unit,
            NotificationWorker.KEY_END_DATE to (endDate ?: -1L)
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = intervalHours.toLong(),
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(calculateInitialDelay(startTime, startDate), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(scheduleId.toString())
            .build()

        //ствоюємо цикл
        workManager.enqueueUniquePeriodicWork(
            scheduleId.toString(),
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

    }
    fun cancelNotifications(scheduleId: Long) {
        workManager.cancelUniqueWork(scheduleId.toString())
    }

    private fun calculateInitialDelay(startTime: String, startDate: Long): Long {
        val now = System.currentTimeMillis()

        //час першого прийому
        val (hours, minutes) = startTime.split(":").map { it.toInt() }

        //перший прийом
        val calendar = Calendar.getInstance().apply {
            timeInMillis = maxOf(now, startDate)
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // якщо час вже минув — переносимо на завтра
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return calendar.timeInMillis - now
    }

    fun scheduleDelayed(
        delayMinutes: Int,
        medicationName: String,
        dosage: Int,
        unit: String
    ) {
        val inputData = workDataOf(
            NotificationWorker.KEY_MEDICATION_NAME to medicationName,
            NotificationWorker.KEY_DOSAGE to dosage,
            NotificationWorker.KEY_UNIT to unit,
            NotificationWorker.KEY_END_DATE to -1L
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("test_notification")
            .build()

        workManager.enqueue(workRequest)
    }
}