package com.buttstuff.localserverwatchdog.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class WatchdogViewModel : BaseViewModel() {

    val isRequiredDataSet: SharedFlow<Boolean> = MutableSharedFlow()

    private val repository = Repository.getInstance()

    init {
        viewModelScope.launch {
            isRequiredDataSet.emit(repository.isRequiredDataSet())
        }
    }
}
