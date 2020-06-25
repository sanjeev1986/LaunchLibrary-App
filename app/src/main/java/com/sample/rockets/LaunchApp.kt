package com.sample.rockets

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.sample.rockets.api.ApiManager
import com.sample.rockets.common.AppViewModerFactory
import com.sample.rockets.platform.PlatformManager
import com.sample.rockets.storage.StorageManager
import com.sample.rockets.utils.network.HttpStack
import timber.log.Timber

class LaunchApp : Application() {
    companion object {
        fun getInstance(context: Context): LaunchApp = context.applicationContext as LaunchApp
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .build()
            )
            Timber.plant(Timber.DebugTree())
        }
        super.onCreate()

    }

    /**
     * Singleton HTTPStack instance
     */
    private val httpStack by lazy(LazyThreadSafetyMode.NONE) {
        HttpStack(
            BuildConfig.BASE_URL,
            cacheDir
        )
    }

    /**
     * Singleton storage provider
     */
    private val storageManager by lazy(LazyThreadSafetyMode.NONE) { StorageManager(this) }

    /**
     * Singleton API Manager
     */
    private val apiManager by lazy(LazyThreadSafetyMode.NONE) { ApiManager(httpStack) }

    /**
     * Singleton ViewModel factory provider
     */
    val appViewModerFactory by lazy {
        AppViewModerFactory(apiManager, platformManager, storageManager)
    }

    /**
     * Singleton platform access
     */
    private val platformManager = PlatformManager(this)

}