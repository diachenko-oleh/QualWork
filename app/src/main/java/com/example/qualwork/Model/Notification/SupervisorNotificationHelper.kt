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

object SupervisorNotificationHelper {

    const val CHANNEL_ID = "supervisor_missed_channel"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMissedNotification(
        context: Context,
        patientName: String,
        medicationName: String,
        time: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Пропуски пацієнтів",
                NotificationManager.IMPORTANCE_HIGH
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Пацієнт $patientName пропустив прийом")
            .setContentText(" Пропущено прийом $medicationName о $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationId = (patientName + medicationName + time).hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}