package com.example.qualwork.Model.Notification

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotifications(
        scheduleId: Long,
        medicationName: String,
        dosage: Int,
        unit: String,
        intakeTimes: List<LocalTime>,
        startDate: Long,
        endDate: Long?,
        medAmount: Int?
    ) {
        cancelNotifications(scheduleId)

        intakeTimes.forEach { time ->
            val delay = calculateInitialDelay(time, startDate)

            val inputData = workDataOf(
                NotificationWorker.KEY_MEDICATION_NAME to medicationName,
                NotificationWorker.KEY_DOSAGE to dosage,
                NotificationWorker.KEY_UNIT to unit,
                NotificationWorker.KEY_END_DATE to (endDate ?: -1L),
                NotificationWorker.KEY_SCHEDULE_ID to scheduleId,
                NotificationWorker.KEY_TIME to time.toString()
            )

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(scheduleId.toString())
                .addTag("${scheduleId}_${time}")
                .build()

            workManager.enqueue(workRequest)
        }
    }
    fun cancelNotifications(scheduleId: Long) {
        workManager.cancelAllWorkByTag(scheduleId.toString())
    }

    private fun calculateInitialDelay(time: LocalTime, startDate: Long): Long {
        val now = ZonedDateTime.now()
        val zone = ZoneId.systemDefault()

        var nextTrigger = Instant.ofEpochMilli(startDate)
            .atZone(zone)
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (nextTrigger.isBefore(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }

        return Duration.between(now, nextTrigger).toMillis()
    }

    fun scheduleDelayed(
        delayMinutes: Int,
        medicationName: String,
        dosage: Int,
        unit: String,
        scheduleId:Long
    ) {
        Log.d("WORK_DEBUG", "scheduleDelayed called")
        Log.d("WORK_DEBUG", "scheduleId = $scheduleId, delay = $delayMinutes")
        val inputData = workDataOf(
            NotificationWorker.KEY_MEDICATION_NAME to medicationName,
            NotificationWorker.KEY_DOSAGE to dosage,
            NotificationWorker.KEY_UNIT to unit,
            NotificationWorker.KEY_END_DATE to -1L,
            NotificationWorker.KEY_SCHEDULE_ID to scheduleId,
            NotificationWorker.KEY_TIME to "08:00"
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("test_notification")
            .build()
        Log.d("WORK_DEBUG", "WorkRequest created: $workRequest")
        Log.d("WORK_DEBUG", "Enqueuing work...")
        workManager.enqueue(workRequest)
    }
}