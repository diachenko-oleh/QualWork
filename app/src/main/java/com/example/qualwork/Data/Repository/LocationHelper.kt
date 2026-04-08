package com.example.qualwork.Data.Repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object LocationHelper {
    suspend fun getCitySlug(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json&accept-language=en"
            val response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .ignoreContentType(true)
                .timeout(5_000)
                .get()
                .body().text()

            val json = org.json.JSONObject(response)
            val address = json.getJSONObject("address")
            val city = address.optString("city")
                .ifEmpty { address.optString("town") }
                .ifEmpty { address.optString("village") }
                .lowercase()
                .trim()
                .replace(" ", "-")

            android.util.Log.d("SCRAPER", "city slug: $city")
            city.ifEmpty { "kyiv" }
        } catch (e: Exception) {
            android.util.Log.e("SCRAPER", "getCitySlug error: ${e.message}")
            "kyiv"
        }
    }

    suspend fun getUserLocation(context: Context): Pair<Double, Double>? {

        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return null
        }

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