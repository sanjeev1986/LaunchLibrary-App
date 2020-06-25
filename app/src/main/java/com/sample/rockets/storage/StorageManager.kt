package com.sample.rockets.storage

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.sample.rockets.storage.disk.DiskCache
import com.sample.rockets.storage.memory.InMemoryCache

class StorageManager(private val application: Application) : ComponentCallbacks2 {

    val memoryCache by lazy(LazyThreadSafetyMode.NONE) { InMemoryCache() }
    val diskCache by lazy(LazyThreadSafetyMode.NONE) { DiskCache(application) }

    override fun onLowMemory() {
        memoryCache.clear()
    }

    override fun onConfigurationChanged(p0: Configuration?) {}

    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            memoryCache.clear()
        }
    }


}