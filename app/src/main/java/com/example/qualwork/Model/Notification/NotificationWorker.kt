package com.example.qualwork.Model.Notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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

        // Якщо курс завершено — зупиняємо Worker
        if (endDate != -1L && System.currentTimeMillis() > endDate) {
            return Result.success()
        }
        try {
            showNotification(context, medicationName, dosage, unit)
            return Result.success()
        } catch (e: SecurityException) {
            return Result.failure()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        context: Context,
        medicationName: String,
        dosage: Int,
        unit: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Прийом ліків",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Нагадування про прийом лікарських засобів"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Час приймати $medicationName")
            .setContentText("Дозування: $dosage $unit")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
           /* .addAction(0, "Прийняв", takeMed)
            .addAction(0, "30 хв пізніше", wait)
            .addAction(0, "Пропустити", skipMed)*/
            .build()

        NotificationManagerCompat.from(context).notify(medicationName.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_channel"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_UNIT = "unit"
        const val KEY_END_DATE = "end_date"
    }
}