package com.buttstuff.localserverwatchdog.ui.screen.settings

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import com.buttstuff.localserverwatchdog.ui.screen.model.UiInterval
import com.buttstuff.localserverwatchdog.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

//todo inverse control on this screen, listen for changes from repository instead of pinging it on screen shown
class SettingsVM : BaseViewModel() {

    val screenState: StateFlow<ScreenState> = MutableStateFlow(ScreenState())

    private val repository = Repository.getInstance()
    private val watchdogManager = WatchdogManager.getInstance()

    fun updateListenOnlyOverWifi(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.enableCanWatchOnlyOverWifi(isEnabled)
            updateData()
        }
    }

    fun updateWatchdogStatus(shouldRun: Boolean) {
        viewModelScope.launch {
            repository.enableWatchdog(isEnabled = shouldRun)
            if (shouldRun) {
                watchdogManager.startWatchdog()
            } else {
                watchdogManager.stopWatchdog()
            }
            updateData()
        }
    }

    fun updateData() {
        viewModelScope.launch {
            screenState.setValue(
                ScreenState(
                    repository.getServerAddress(),
                    parseInterval(repository.getInterval()),
                    repository.canWatchOnlyOverWifi(),
                    watchdogManager.isWatchdogRunning()
                )
            )
        }
    }

    private fun parseInterval(millis: Long): UiInterval {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return UiInterval(hours.toInt(), minutes.toInt())
    }
}

data class ScreenState(
    val serverAddress: String = "",
    val interval: UiInterval = UiInterval(hours = 0, minutes = 0),
    val runOnlyOnWifi: Boolean = true,
    val isWatchdogCurrentlyRunning: Boolean = true
)
