package com.sample.rockets.launch.list

import com.sample.rockets.api.Launch
import com.sample.rockets.api.LaunchApi
import com.sample.rockets.api.LaunchResponse
import com.sample.rockets.common.BaseViewModel
import com.sample.rockets.common.ViewModelResult
import com.sample.rockets.common.ViewModelResult.Failure
import com.sample.rockets.common.ViewModelResult.Success
import com.sample.rockets.platform.NetworkManager
import com.sample.rockets.storage.disk.DiskCache
import com.sample.rockets.storage.memory.InMemoryCache
import io.reactivex.Maybe

/**
 * ViewModle to manage launch data fetch and cache
 */
class LaunchListViewModel(
    private val networkManager: NetworkManager,
    private val memoryCache: InMemoryCache,
    private val diskCache: DiskCache,
    private val api: LaunchApi
) : BaseViewModel<List<Launch>>() {

    /**
     * Fetch the top 10 launch list. set refresh = true to force fetch API and update cache
     */
    fun fetchLaunchList(refresh: Boolean = false) {

        if (refresh) {//Force fetch from Api And update different cache levels
            resultsLiveData.value = ViewModelResult.Progress
            disposables.add(networkManager.checkNetworkAndDispatch { api.getTenUpcomingLaunches() }.doOnSuccess {
                memoryCache.saveToInMemoryCache(
                    LaunchListViewModel::class.java.simpleName,
                    it
                )//update memory cache for faster load
                disposables.add(
                    diskCache.saveFile(LaunchListViewModel::class.java.simpleName, it)
                        .subscribe({}, {})
                )
            }.map { it.launches }
                .subscribe({
                    resultsLiveData.value = Success(it)
                }, {
                    resultsLiveData.value = Failure(it)
                }))
        } else {
            disposables.add(
                memoryCache.getDataFromCache<LaunchResponse>(LaunchListViewModel::class.java.simpleName)//Try in memory cache
                    .switchIfEmpty(Maybe.defer {
                        //Switch to Disk cache of memory cache is empty
                        diskCache.readFile(
                            LaunchListViewModel::class.java.simpleName,
                            LaunchResponse::class.java
                        ).doOnSuccess {
                            memoryCache.saveToInMemoryCache(
                                LaunchListViewModel::class.java.simpleName,
                                it
                            )//update memory cache for faster load
                        }
                    })
                    .switchIfEmpty(
                        //Switch to Api call if Disk cache is empty
                        networkManager.checkNetworkAndDispatch {
                            resultsLiveData.value = ViewModelResult.Progress
                            api.getTenUpcomingLaunches()
                        }.doOnSuccess {
                            //update different cache levels
                            memoryCache.saveToInMemoryCache(LaunchListViewModel::class.java.simpleName, it)
                            disposables.add(
                                diskCache.saveFile(LaunchListViewModel::class.java.simpleName, it)
                                    .subscribe({}, {})
                            )
                        }
                    )
                    .map { it.launches }//extract and deliver relevant data
                    .subscribe({
                        resultsLiveData.value = Success(it)
                    }, {
                        resultsLiveData.value = Failure(it)
                    })
            )
        }


    }

    /**
     * Search from the cache, does not need distinctUntilChanged & debounce since it does not invoke API call
     */
    fun search(text: String) {
        disposables.add(memoryCache.getDataFromCache<LaunchResponse>(LaunchListViewModel::class.java.simpleName)
            .map {
                it.launches.filter { launch -> launch.missions.firstOrNull()?.name?.contains(text, true) ?: false }
            }.subscribe({
                resultsLiveData.value = Success(it)
            }, {
                resultsLiveData.value = Failure(it)
            })
        )
    }
}