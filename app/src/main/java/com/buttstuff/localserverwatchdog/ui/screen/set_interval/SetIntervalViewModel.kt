package com.buttstuff.localserverwatchdog.ui.screen.set_interval

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SetIntervalViewModel : BaseViewModel() {

    val screenState: StateFlow<ScreenState> = MutableStateFlow(ScreenState())
    val sideEffect: SharedFlow<SideEffect> = MutableSharedFlow()

    private val repository = Repository.getInstance()
    private val interval = IntervalBuilder()

    init {
        viewModelScope.launch {
            interval.set(repository.getInterval())
            screenState.setValue(screenState.value.copy(interval = interval.buildUI()))
        }
    }

    fun onCommand(command: Command) {
        viewModelScope.launch {
            if (command is Done && interval.isValid()) {
                repository.saveIntervalInMillis(interval.toMillis())
                sideEffect.emit(SetInterval)
            }
            interval.handleCommand(command)
            screenState.setValue(screenState.value.copy(interval = interval.buildUI()))
        }
    }
}

data class ScreenState(val interval: UiInterval = UiInterval(0, 0))

interface SideEffect
object SetInterval : SideEffect

data class UiInterval(val hours: Int, val minutes: Int) {
    val isEmpty: Boolean get() = hoursEmpty && minutes == 0
    val hoursEmpty: Boolean get() = hours == 0

}