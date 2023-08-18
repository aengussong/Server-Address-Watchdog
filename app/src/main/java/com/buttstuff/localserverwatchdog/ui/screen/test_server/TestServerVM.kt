package com.buttstuff.localserverwatchdog.ui.screen.test_server

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import com.buttstuff.localserverwatchdog.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TestServerVM : BaseViewModel() {

    val screenState: StateFlow<ScreenState> = MutableStateFlow(ScreenState())

    private val watchdogManager = WatchdogManager.getInstance()
    private val repository = Repository.getInstance()

    init {
        viewModelScope.launch {
            watchdogManager.checkServerAndScheduleNextCheckup()
            screenState.setValue(screenState.value.copy(lastCheckupResult = repository.getLastCheckupData()))
        }
    }

    fun testServer() {
        viewModelScope.launch {
            screenState.setValue(screenState.value.copy(isProgressShown = true))

            watchdogManager.checkServerOnce()

            screenState.setValue(
                screenState.value.copy(
                    isProgressShown = false,
                    lastCheckupResult = repository.getLastCheckupData()
                )
            )
        }
    }
}

data class ScreenState(val lastCheckupResult: String = "", val isProgressShown: Boolean = false)
