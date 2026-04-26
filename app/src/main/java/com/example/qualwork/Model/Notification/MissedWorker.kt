package com.example.qualwork.Model.Notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalTime

@HiltWorker
class MissedWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val intakeLogDao: IntakeLogDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("MISSED_DEBUG", "MissedWorker doWork() called!")
            val scheduleId = inputData.getLong("scheduleId", -1L)
            val timeString = inputData.getString("time") ?: return Result.failure()

            Log.d("MISSED_DEBUG", "MissedWorker started: scheduleId=$scheduleId, time=$timeString")

            if (scheduleId == -1L){
                Log.d("MISSED_DEBUG", "Invalid input, failure")
                return Result.failure()
            }

            val plannedDateTime = LocalDate.now().atTime(LocalTime.parse(timeString))
            val plannedDateStr = plannedDateTime.toString().substring(0, 16)

            Log.d("MISSED_DEBUG", "plannedDateTime=$plannedDateTime, plannedDateStr=$plannedDateStr")

            val takenCount = intakeLogDao.isTaken(scheduleId, plannedDateStr)
            val skippedCount = intakeLogDao.isSkipped(scheduleId, plannedDateStr)

            Log.d("MISSED_DEBUG", "takenCount=$takenCount, skippedCount=$skippedCount")

            val alreadyLogged = intakeLogDao.isTaken(scheduleId, plannedDateStr) > 0
                    || intakeLogDao.isSkipped(scheduleId, plannedDateStr) > 0

            if (!alreadyLogged) {
                Log.d("MISSED_DEBUG", "No log found, saving as skipped and sending notification")
                intakeLogDao.insert(
                    IntakeLog(
                        scheduleId = scheduleId,
                        plannedDoseTime = plannedDateTime,
                        actualDoseTime = null,
                        taken = false
                    )
                )
                // надсилаємо повідомлення
                showMissedNotification(scheduleId, timeString)
            }else {
                Log.d("MISSED_DEBUG", "Already logged, skipping notification")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("MISSED_DEBUG", "MissedWorker FAILED: ${e.message}", e)
            Result.failure()
        }
    }

    private fun showMissedNotification(scheduleId: Long, timeString: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "missed_channel",
                "Пропущені прийоми",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "missed_channel")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Прийом пропущено")
            .setContentText("Ви не прийняли ліки о $timeString")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context)
                .notify(scheduleId.toInt() * 100 + timeString.hashCode() + 1, notification)
        }
    }
}