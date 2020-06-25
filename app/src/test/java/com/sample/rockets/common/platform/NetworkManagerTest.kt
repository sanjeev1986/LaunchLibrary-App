package com.sample.rockets.common.platform

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.sample.rockets.platform.NetworkManager
import com.sample.rockets.platform.NetworkStatus
import com.sample.rockets.platform.NoConnectivity
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class NetworkManagerTest {

    private val connectivityManager = mockk<ConnectivityManager>()
    private val application = mockk<Application>().also { appContext ->
        every { appContext.getSystemService(eq(Context.CONNECTIVITY_SERVICE)) } returns connectivityManager
    }
    private val networkManager =
        NetworkManager(application)

    @Test
    fun check_connected_to_mobile_network_behavior() {
        every { connectivityManager.activeNetworkInfo } returns mockk<NetworkInfo>().also {
            every { it.isConnected } returns true
            every { it.type } returns ConnectivityManager.TYPE_MOBILE
        }
        networkManager.getNetworkStatus()
            .test()
            .assertValue(
                NetworkStatus(
                    true,
                    ConnectivityManager.TYPE_MOBILE
                )
            )
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun check_connected_to_wifi_behavior() {
        every { connectivityManager.activeNetworkInfo } returns mockk<NetworkInfo>().also {
            every { it.isConnected } returns true
            every { it.type } returns ConnectivityManager.TYPE_WIFI
        }
        networkManager.getNetworkStatus()
            .test()
            .assertValue(
                NetworkStatus(
                    true,
                    ConnectivityManager.TYPE_WIFI
                )
            )
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun check_disconnected_to_network_behavior() {
        every { connectivityManager.activeNetworkInfo } returns null
        networkManager.getNetworkStatus()
            .test()
            .assertError(NoConnectivity)
            .assertNoValues()
    }

}