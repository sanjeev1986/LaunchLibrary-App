package com.sample.rockets.api

import com.sample.rockets.utils.network.HttpStack

class ApiManager(httpStack: HttpStack) {
    /**
     * Access launch list API
     */
    val launchApi by lazy(LazyThreadSafetyMode.NONE) {
        LaunchApi(
            httpStack
        )
    }
}