package com.sample.rockets.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sample.rockets.api.ApiManager
import com.sample.rockets.launch.list.LaunchListViewModel
import com.sample.rockets.platform.PlatformManager
import com.sample.rockets.storage.StorageManager


/**
 * View model Abstract factory
 */
class AppViewModerFactory(
    private val apiManager: ApiManager,
    private val platformManager: PlatformManager,
    private val storageManager: StorageManager
) {


    companion object {
        private var testInstance: ViewModelProvider.Factory? = null
        /**
         * Set this instance for Espresso testing
         */
        fun setInstance(mock: ViewModelProvider.Factory?) {
            testInstance = mock
        }
    }

    /**
     * create a factory for LauncListViewModel
     */
    @Suppress("UNCHECKED_CAST")
    fun newLaunchListViewModelFactory(): ViewModelProvider.Factory =
        testInstance ?: object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LaunchListViewModel(
                    platformManager.networkManager,
                    storageManager.memoryCache,
                    storageManager.diskCache,
                    apiManager.launchApi
                ) as T
            }
        }
}