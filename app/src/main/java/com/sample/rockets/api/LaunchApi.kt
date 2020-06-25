package com.sample.rockets.api

import com.sample.rockets.utils.network.HttpStack
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Launch List API
 */
class LaunchApi(private val httpStack: HttpStack) {

    private val api by lazy(LazyThreadSafetyMode.NONE) { httpStack.constructApiFacade<RetrofitLaunchApi>() }

    fun getTenUpcomingLaunches(): Single<LaunchResponse> {
        return httpStack.dispatchHttpRequest(api.fetchNextTenLaunches())
    }

    /**
     * Exists as a separate internal interface to
     * decouple retrofit specifics from the parent(LaunchApi) domain object
     */
    private interface RetrofitLaunchApi {
        @GET("launch/next/10")
        fun fetchNextTenLaunches(): Single<LaunchResponse>
    }

}