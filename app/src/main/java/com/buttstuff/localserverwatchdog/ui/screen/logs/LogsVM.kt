package com.buttstuff.localserverwatchdog.ui.screen.logs

import androidx.lifecycle.viewModelScope
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogsVM : BaseViewModel() {

    val data: StateFlow<List<String>> = MutableStateFlow(emptyList())

    private val repository = Repository.getInstance()

    //todo inverse control, listen for changes in file instead of peeking into it from time to time
    fun updateData() {
        viewModelScope.launch {
            data.setValue(repository.getFullLogs())
        }
    }
}
