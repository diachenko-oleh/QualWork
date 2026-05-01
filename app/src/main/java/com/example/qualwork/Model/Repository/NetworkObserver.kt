package com.example.qualwork.Model.Repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.qualwork.ViewModel.NetworkUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkObserver(private val context: Context) {

    val isOnline: Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        // Початковий стан
        trySend(NetworkUtils.isOnline(context))

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}

enum class NetworkBannerState {
    HIDDEN,
    OFFLINE,
    SYNCING
}

data class stateContainer<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)