@file:Suppress("UNCHECKED_CAST")

package com.sample.rockets.storage.memory

import android.util.LruCache
import io.reactivex.Maybe

/**
 * In memory LRU cache used to store reusable data models.
 * Not used in the app. Was using this for faster data fetch i.e memory->network
 * Since Android Pagination has a cache of its own, This ended up being redundant
 */
class InMemoryCache(size: Int = 1 * 1024 * 1024/* Default 1 MB cache*/) {
    private val memoryCache = LruCache<String, Any>(size)
    fun <T> getDataFromCache(key: String): Maybe<T> {
        val data = memoryCache[key]
        return if (data != null)
            Maybe.just(data as T)
        else
            Maybe.empty()
    }

    fun <T> saveToInMemoryCache(key: String, data: T): T {
        memoryCache.put(key, data)
        return data
    }


    fun <T> removeFromCache(key: String) = memoryCache.remove(key) as T

    fun clear() = memoryCache.evictAll()

}