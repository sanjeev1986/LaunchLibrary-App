package com.sample.rockets.utils.network

import com.sample.rockets.utils.network.CONNECTION_TIMEOUT_SECS
import com.sample.rockets.utils.network.SOCKET_TIMEOUT_SECS
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Generic HTTP stack boiler plate. One instance per Domain
 */
class HttpStack(
    private val baseUrl: String,
    private val cacheDir: File
) {
    private val networkInterceptors: MutableList<Interceptor> =
        mutableListOf(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    private val appInterceptorList: MutableList<Interceptor> = mutableListOf()
    private val okhttpClient: OkHttpClient by lazy {
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cache = Cache(cacheDir, cacheSize)
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT_SECS, TimeUnit.SECONDS)
            .readTimeout(SOCKET_TIMEOUT_SECS, TimeUnit.SECONDS)
            .writeTimeout(SOCKET_TIMEOUT_SECS, TimeUnit.SECONDS)
            .cache(cache)
        networkInterceptors.forEach {
            builder.addInterceptor(it)
        }
        appInterceptorList.forEach {
            builder.addNetworkInterceptor(it)
        }
        builder.build()
    }

    val retrofit: Retrofit by lazy {
        val retrofitBuilder = Retrofit.Builder()
        retrofitBuilder
            .baseUrl(baseUrl)
            .client(okhttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Helper to create retrofit interface for cleaner syntax
     */
    inline fun <reified T> constructApiFacade(): T = retrofit.create(T::class.java)

    /**
     * Dispatch a simple http request
     */
    fun <T> dispatchHttpRequest(retrofitSingle: Single<T>): Single<T> {
        return retrofitSingle.compose { observable ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * Left empty to demonstrate capabilites of HTTPStack
     */
    fun <T> dispatchPeriodicSync(retrofitSingle: Single<T>) {

    }

    /**
     * Left empty to demonstrate capabilites of HTTPStack
     */
    fun <T> dispatchPrefetch(retrofitSingle: Single<T>) {

    }
}