package com.sample.rockets.platform

import android.app.Application
import android.content.Context

import android.net.ConnectivityManager
import io.reactivex.Single

/**
 * Connectivity manager abstraction
 */
class NetworkManager(private val application: Application) {

    private val connectivityManager by lazy { application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    /**
     * Return current network status
     */
    fun getNetworkStatus(): Single<NetworkStatus> {
        return connectivityManager.activeNetworkInfo?.let {
            if (connectivityManager.activeNetworkInfo.isConnected) {
                Single.just(
                    NetworkStatus(
                        it.isConnected,
                        it.type
                    )
                )
            } else {
                Single.error(NoConnectivity)
            }

        } ?: Single.error(NoConnectivity)
    }

    /**
     * Helper(a functional paradigm) to check network and dispatch an network call
     */
    inline fun <T> checkNetworkAndDispatch(crossinline networkBlock: () -> Single<T>): Single<T> =
        Single.defer { getNetworkStatus() }.flatMap { networkBlock() }

}

/**
 * Not connected to internet
 */
object NoConnectivity : Exception()

data class NetworkStatus(val isConnected: Boolean, val networkType: Int)
