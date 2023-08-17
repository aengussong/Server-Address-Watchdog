package com.buttstuff.localserverwatchdog.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

open class BaseViewModel : ViewModel() {
    protected suspend fun <T> SharedFlow<T>.emit(value: T) {
        (this as MutableSharedFlow<T>).emit(value)
    }
}
