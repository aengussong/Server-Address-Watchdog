package com.buttstuff.localserverwatchdog.data

import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.data.local.LocalData
import com.buttstuff.localserverwatchdog.data.local.WatchdogSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository private constructor(private val localData: LocalData) {

    suspend fun isRequiredDataSet() = withContext(Dispatchers.IO) {
        localData.serverAddress.isNotBlank()
    }

    suspend fun getServerAddress() = withContext(Dispatchers.IO) {
        localData.serverAddress
    }

    suspend fun saveServerAddress(serverAddress: String) = withContext(Dispatchers.IO) {
        localData.serverAddress = serverAddress
    }

    companion object {
        private var instance: Repository? = null
        fun getInstance() =
            instance ?: Repository(WatchdogSharedPreferences.getInstance(WatchdogApplication.appContext)).also {
                instance = it
            }
    }
}
