package com.example.qualwork.Model.Notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Repository.IntakeLogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@HiltWorker
class MissedWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val intakeRepository: IntakeLogRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MISSED_DEBUG", "MissedWorker doWork() called!")
        val scheduleId = inputData.getLong("scheduleId", -1L)
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()

        val plannedDateTime = inputData.getString("time")
            ?.let { LocalDateTime.parse(it) }
            ?: return Result.failure()

        Log.d("MISSED_DEBUG", "MissedWorker started: scheduleId=$scheduleId, time=$plannedDateTime")

        if (scheduleId == -1L){
            Log.d("MISSED_DEBUG", "Invalid input, failure")
            return Result.failure()
        }

        val wasTaken = intakeRepository.checkIfTaken(scheduleId, plannedDateTime)
        Log.d("MISSED_DEBUG", "wasTaken=$wasTaken")

        if (!wasTaken) {
            showMissedNotification(medicationName,plannedDateTime.toLocalTime(), scheduleId)
        }
       return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showMissedNotification(
        medicationName: String,
        time: LocalTime,
        scheduleId: Long
    ) {
        val displayTime = String.format("%02d:%02d", time.hour, time.minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MISSED_CHANNEL_ID,
                "Пропущені прийоми",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(context, MISSED_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Пропущено прийом")
            .setContentText("Пропущено прийом $medicationName за $displayTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationId = (scheduleId * 1000 + time.hashCode()).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    companion object {
        const val MISSED_CHANNEL_ID = "missed_medication_channel"
    }
}