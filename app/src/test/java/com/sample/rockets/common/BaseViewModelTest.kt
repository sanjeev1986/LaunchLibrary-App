package com.sample.rockets.common

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sample.rockets.common.BaseViewModel
import com.sample.rockets.common.ViewModelResult
import com.sample.rockets.platform.NoConnectivity
import com.google.common.truth.Truth
import org.junit.Rule
import org.junit.Test

class BaseViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private class TestViewModel : BaseViewModel<String>() {
        fun setSuccess(data: String) {
            resultsLiveData.value =
                ViewModelResult.Success(data)
        }

        fun setFailure(exception: Exception) {
            resultsLiveData.value =
                ViewModelResult.Failure(exception)
        }
    }

    private val viewModel =
        TestViewModel()

    @Test
    fun check_success_behavior() {
        viewModel.setSuccess("Test")
        Truth.assertThat((viewModel.uiLiveData.value as ViewModelResult.Success).result).isEqualTo("Test")
    }

    @Test
    fun check_failure_behavior() {
        viewModel.setFailure(NoConnectivity)
        Truth.assertThat((viewModel.uiLiveData.value as ViewModelResult.Failure).error).isEqualTo(NoConnectivity)
    }
}