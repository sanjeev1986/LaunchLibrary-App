package com.sample.rockets.platform

import android.app.Application

/**
 * Access all Android platform capabilities like Connectivity, AlarmManager etc using this Facade
 */
class PlatformManager(application: Application) {
    val networkManager by lazy { NetworkManager(application) }
}