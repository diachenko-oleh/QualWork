package com.example.qualwork.Model.Notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qualwork.Model.Repository.FirestoreRepository
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

@HiltWorker
class MissedWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val intakeRepository: IntakeLogRepository,
    private val firestoreRepository: FirestoreRepository,
    //private val userPreferences: UserPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong("scheduleId", -1L)
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()

        val plannedDateTime = inputData.getString("time")
            ?.let { LocalDateTime.parse(it) }
            ?: return Result.failure()

            //Log.d("MISSED_DEBUG", "MissedWorker started: scheduleId=$scheduleId, time=$plannedDateTime")

        if (scheduleId == -1L) return Result.failure()

        val wasTaken = intakeRepository.checkIfTaken(scheduleId, plannedDateTime)
            //Log.d("MISSED_DEBUG", "wasTaken=$wasTaken")

        if (!wasTaken) {
            intakeRepository.logMissedIntake(
                scheduleId = scheduleId,
                plannedTime = plannedDateTime.toLocalTime()
            )
            showMissedNotification(medicationName,plannedDateTime.toLocalTime(), scheduleId)

            val userId = inputData.getString("userId") ?: return Result.success()
            val userName = inputData.getString("userName") ?: return Result.success()
            val displayTime = String.format("%02d:%02d",plannedDateTime.hour,plannedDateTime.minute)

            firestoreRepository.notifySupervisors(
                patientId = userId,
                patientName = userName,
                medicationName = medicationName,
                time = displayTime
            )
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