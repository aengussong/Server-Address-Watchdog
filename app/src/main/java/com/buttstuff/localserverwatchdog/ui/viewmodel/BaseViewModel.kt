package com.buttstuff.localserverwatchdog.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel() {

    protected fun <T> StateFlow<T>.setValue(value: T) {
        (this as MutableStateFlow).value = value
    }

    protected suspend fun <T> SharedFlow<T>.emit(value: T) {
        (this as MutableSharedFlow<T>).emit(value)
    }
}
