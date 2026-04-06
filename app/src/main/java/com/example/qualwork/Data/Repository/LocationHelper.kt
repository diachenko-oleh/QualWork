package com.example.qualwork.Data.Repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(context: Context): Pair<Double, Double>? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        //остання локация
        val lastLocation = fusedClient.lastLocation.await()
        if (lastLocation != null) {
            return Pair(lastLocation.latitude, lastLocation.longitude)
        }

        //шукаємо нову
        val freshLocation = fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()

        return if (freshLocation != null) {
            Pair(freshLocation.latitude, freshLocation.longitude)
        } else {
            null
        }
    }
}