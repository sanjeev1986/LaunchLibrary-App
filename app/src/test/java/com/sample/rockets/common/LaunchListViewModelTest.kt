package com.sample.rockets.common

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sample.rockets.launch.list.LaunchListViewModel
import com.sample.rockets.platform.NetworkManager
import com.sample.rockets.platform.NetworkStatus
import com.sample.rockets.platform.NoConnectivity
import com.sample.rockets.storage.disk.DiskCache
import com.sample.rockets.storage.memory.InMemoryCache
import com.google.common.truth.Truth
import com.sample.rockets.api.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test

class LaunchListViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val dataStub = LaunchResponse().apply {
        launches = mutableListOf(Launch().apply {
            id = 1679
            name = "Soyuz 2.1b/Fregat-M | Glonass-M No. 58"
            wsstamp = 1558936800
            location = Location().apply {
                pads = mutableListOf(Pad().apply {
                    latitude = 62.92883
                    longitude = 40.457098
                })
            }
            rocket = Rocket().apply {
                id = 153
                name = "Soyuz 2.1b/Fregat-M"
            }
            missions = listOf(Mission().apply {
                id = 1207
                name = "Glonass-M No. 58"
                description =
                    "Glonass-M, also known as Uragan-M, are the second generation of Uragan satellite design used for GLONASS satellite navigation system. GLONASS is a Russian space-based navigation system comparable to the similar GPS and Galileo systems. This generation improves on accuracy, power consumption and design life. Each satellite weighs 1415 kg, is equipped with 12 L-band antennas, and has an operational lifetime of 7 years."
                agencies = listOf(Agency().apply {
                    id = 1207
                    name = "Russian Federal Space Agency (ROSCOSMOS)"
                })
            })
        })
    }

    private val memoryCache = mockk<InMemoryCache>(relaxed = true).also { cache ->
        every {
            cache.getDataFromCache<LaunchResponse>(eq(LaunchListViewModel::class.java.simpleName))
        } returns Maybe.just(
            dataStub
        )
    }
    private val diskCache = mockk<DiskCache>(relaxed = true).also { cache ->
        every {
            cache.readFile(eq(LaunchListViewModel::class.java.simpleName), eq(LaunchResponse::class.java))
        } returns Maybe.just<LaunchResponse>(
            dataStub
        )
    }

    private val api = mockk<LaunchApi>(relaxed = true).also { api ->
        every {
            api.getTenUpcomingLaunches()
        } returns Single.just(
            mockk(relaxed = true)
        )
    }
    private val networkManager = mockk<NetworkManager>(relaxed = true).also { networkManager ->
        every { networkManager.getNetworkStatus() } returns Single.just(NetworkStatus(true, 1))
    }

    private val viewModel = LaunchListViewModel(networkManager, memoryCache, diskCache, api)

    @Test
    fun check_error_handling_when_no_connectivity() {
        every { networkManager.getNetworkStatus() } returns Single.error(NoConnectivity)
        viewModel.fetchLaunchList(true)
        Truth.assertThat(viewModel.uiLiveData.value is ViewModelResult.Failure).isTrue()
        verify(exactly = 0) { api.getTenUpcomingLaunches() }
    }

    @Test
    fun check_api_is_called_and_updates_cache_when_no_cache() {
        viewModel.fetchLaunchList(true)
        Truth.assertThat(viewModel.uiLiveData.value is ViewModelResult.Success).isTrue()
        verify(exactly = 1) { api.getTenUpcomingLaunches() }
        verify(exactly = 1) {
            memoryCache.saveToInMemoryCache(
                eq(LaunchListViewModel::class.java.simpleName),
                any<LaunchResponse>()
            )
        }
        verify(exactly = 1) {
            diskCache.saveFile(
                eq(LaunchListViewModel::class.java.simpleName),
                any<LaunchResponse>()
            )
        }
    }

    @Test
    fun check_in_memory_cache_hit() {
        viewModel.fetchLaunchList()
        Truth.assertThat(viewModel.uiLiveData.value is ViewModelResult.Success).isTrue()
        verify(exactly = 0) { api.getTenUpcomingLaunches() }
        verify(exactly = 0) {
            diskCache.readFile(eq(LaunchListViewModel::class.java.simpleName), eq(LaunchResponse::class.java))
        }
    }

    @Test
    fun check_disk_cache_hit() {
        every {
            memoryCache.getDataFromCache<LaunchResponse>(eq(LaunchListViewModel::class.java.simpleName))
        } returns Maybe.empty()
        viewModel.fetchLaunchList()
        Truth.assertThat(viewModel.uiLiveData.value is ViewModelResult.Success).isTrue()
        verify(exactly = 0) { api.getTenUpcomingLaunches() }
        verify(exactly = 1) {
            diskCache.readFile(eq(LaunchListViewModel::class.java.simpleName), eq(LaunchResponse::class.java))
        }
    }

    @Test
    fun check_search_filter_for_valid_input() {
        viewModel.search("Glo")
        val success = viewModel.uiLiveData.value as ViewModelResult.Success<List<Launch>>
        Truth.assertThat(success.result.find { it.name == "Soyuz 2.1b/Fregat-M | Glonass-M No. 58" }).isNotNull()
    }

    @Test
    fun check_search_filter_for_invalid_input() {
        viewModel.search("zzz")
        val success = viewModel.uiLiveData.value as ViewModelResult.Success<List<Launch>>
        Truth.assertThat(success.result).isEmpty()
    }
}
