package com.sample.rockets.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * All results delivered to View are held in this type
 */
sealed class ViewModelResult<out Result, out Error : Throwable> {
    data class Success<out R>(val result: R) : ViewModelResult<R, Nothing>()
    data class Failure<out E : Throwable>(val error: E) : ViewModelResult<Nothing, E>()
    object Progress : ViewModelResult<Nothing, Nothing>()
}

/**
 * Base View Model which has to be inherited
 */
abstract class BaseViewModel<T> : ViewModel() {

    protected val resultsLiveData = MediatorLiveData<ViewModelResult<T, Throwable>>()
    val uiLiveData: LiveData<ViewModelResult<T, Throwable>>
        get() = resultsLiveData
    protected val disposables = CompositeDisposable()


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}