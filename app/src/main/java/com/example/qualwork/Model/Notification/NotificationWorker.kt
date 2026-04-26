package com.example.qualwork.Model.Notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.qualwork.View.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: return Result.failure()
        val dosage = inputData.getInt(KEY_DOSAGE, 1)
        val unit = inputData.getString(KEY_UNIT) ?: ""
        val endDate = inputData.getLong(KEY_END_DATE, -1L)
        val scheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1L)
        val timeString = inputData.getString(KEY_TIME) ?: return Result.failure()


        scheduleMissedCheck(
            scheduleId,
            LocalDateTime.now().toString(), medicationName
        )

        if (endDate != -1L && System.currentTimeMillis() > endDate) {
            return Result.success()
        }
        try {
            showNotification(medicationName, dosage, unit, scheduleId, timeString)
            return Result.success()
        } catch (e: SecurityException) {
            return Result.failure()
        }
    }
    private fun scheduleMissedCheck(scheduleId: Long, time: String, medicationName: String) {
        Log.d("MISSED_DEBUG", "Scheduling missed check: scheduleId=$scheduleId, time=$time")

        val inputData = workDataOf(
            "scheduleId" to scheduleId,
            "time" to time,
            "medicationName" to medicationName
        )

        val work = OneTimeWorkRequestBuilder<MissedWorker>()
            .setInitialDelay(10, TimeUnit.MINUTES) //повідомленняЧерез
            .setInputData(inputData)
            .addTag("missed_$scheduleId")
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        medicationName: String,
        dosage: Int,
        unit: String,
        scheduleId: Long,
        timeString: String
    )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Прийом ліків",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Нагадування про прийом лікарського засобу"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }



        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("openIntake", true)
            putExtra("scheduleId", scheduleId)
            putExtra("time", timeString)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //Log.d("INTAKE_DEBUG", "Creating notification scheduleId = $scheduleId")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Час приймати $medicationName")
            .setContentText("Дозування: $dosage $unit")
            .setContentText("Натисніть на повідомлення для деталей")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        Log.d("INTAKE_DEBUG", "Worker sending timeString = $timeString, scheduleId = $scheduleId")
        val notificationId = scheduleId.toInt() * 100 + timeString.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_channel"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_UNIT = "unit"
        const val KEY_END_DATE = "end_date"
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_TIME = "time"
    }
}