package com.sample.rockets

import android.widget.EditText
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sample.rockets.R
import com.sample.rockets.common.AppViewModerFactory
import com.sample.rockets.matchers.OnRecyclerView
import com.sample.rockets.RecyclerViewItemCountAssertion
import com.sample.rockets.platform.NetworkManager
import com.sample.rockets.platform.NetworkStatus
import com.sample.rockets.platform.NoConnectivity
import com.sample.rockets.storage.disk.DiskCache
import com.sample.rockets.storage.memory.InMemoryCache
import com.sample.rockets.api.*
import com.sample.rockets.launch.list.LaunchListActivity
import com.sample.rockets.launch.list.LaunchListViewModel
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LaunchListActivityTest {
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
        }, Launch().apply {
            id = 1649
            name = "Proton-M/Briz-M | Yamal-601"
            wsstamp = 1559238120
            location = Location().apply {
                pads = mutableListOf(Pad().apply {
                    latitude = 46.039984
                    longitude = 63.032093
                })
            }
            rocket = Rocket().apply {
                id = 4
                name = "Proton-M/Briz-M"
            }
            missions = listOf(Mission().apply {
                id = 892
                name = "Yamal-601"
                description =
                    "Yamal-601 is a geostationary communications satellite for Gazprom Space Systems."
                agencies = listOf(Agency().apply {
                    id = 273
                    name = "Gazprom Space Systems"
                })
            })
        })
    }
    private val memoryCache = mockk<InMemoryCache>(relaxed = true).also { cache ->
        every {
            cache.getDataFromCache<LaunchResponse>(eq(LaunchListViewModel::class.java.simpleName))
        } returns Maybe.empty()
    }
    private val diskCache = mockk<DiskCache>(relaxed = true).also { cache ->
        every {
            cache.readFile(eq(LaunchListViewModel::class.java.simpleName), eq(LaunchResponse::class.java))
        } returns Maybe.empty()
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
    @get:Rule
    var mCountingTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppViewModerFactory.setInstance(object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LaunchListViewModel(
                    networkManager,
                    memoryCache,
                    diskCache,
                    api
                ) as T
            }
        })
    }

    @After
    fun tearDown() {
        AppViewModerFactory.setInstance(null)
        diskCache.deleteCache(LaunchListViewModel::class.java.simpleName).compose {
            it.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
        }.subscribe()
    }

    @Test
    fun check_no_connectivity_displays_error() {
        every { networkManager.getNetworkStatus() } returns Single.error(NoConnectivity)
        val scenario = launchActivity<LaunchListActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_network)))
    }

    @Test
    fun check_data_is_displayed() {
        every {
            memoryCache.getDataFromCache<LaunchResponse>(eq(LaunchListViewModel::class.java.simpleName))
        } returns Maybe.just(dataStub)
        val scenario = launchActivity<LaunchListActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(OnRecyclerView(0, R.id.launchListView, R.id.launchTitle))
            .check(matches(withText("Soyuz 2.1b/Fregat-M")))
        onView(OnRecyclerView(1, R.id.launchListView, R.id.launchTitle))
            .check(matches(withText("Proton-M/Briz-M")))
    }

    @Test
    fun check_search_works() {
        every {
            memoryCache.getDataFromCache<LaunchResponse>(eq(LaunchListViewModel::class.java.simpleName))
        } returns Maybe.just(dataStub)
        val scenario = launchActivity<LaunchListActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.action_search)).perform(click())
        onView(isAssignableFrom(EditText::class.java)).perform(typeText("Glonass"), pressImeActionButton())
        onView(withId(R.id.launchListView)).check(RecyclerViewItemCountAssertion(1))
        onView(OnRecyclerView(0, R.id.launchListView, R.id.launchTitle))
            .check(matches(withText("Soyuz 2.1b/Fregat-M")))
    }


}