package com.buttstuff.localserverwatchdog.ui.screen.set_ip

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.domain.ServerAddressValidator
import com.buttstuff.localserverwatchdog.ui.viewmodel.BaseViewModel
import com.buttstuff.localserverwatchdog.util.ResultObject
import com.buttstuff.localserverwatchdog.util.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SetServerAddressViewModel : BaseViewModel() {

    val screenState: StateFlow<ScreenState> = MutableStateFlow(ScreenState())

    private val serverAddressValidator = ServerAddressValidator()
    private val repository = Repository.getInstance()

    fun onServerAddressInputChanged(input: String) {
        val newState = screenState.value.copy(serverAddress = input)
        screenState.setValue(newState)
    }

    fun validateServerAddress() {
        viewModelScope.launch {
            //todo double calling screenState is ugly, make something better
            screenState.setValue(screenState.value.copy(isProgressShown = true))
            val validationResult = serverAddressValidator.validate(screenState.value.serverAddress)
            if (validationResult.isSuccess()) repository.saveServerAddress(screenState.value.serverAddress)
            screenState.setValue(screenState.value.copy(isProgressShown = false, validationResult = validationResult))
        }
    }
}

data class ScreenState(
    val serverAddress: String = "",
    val validationResult: ResultObject? = null,
    val isProgressShown: Boolean = false
)