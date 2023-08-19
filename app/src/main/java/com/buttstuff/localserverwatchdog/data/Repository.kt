package com.buttstuff.localserverwatchdog.data

import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.data.local.LocalData
import com.buttstuff.localserverwatchdog.data.local.WatchdogSharedPreferences
import com.buttstuff.localserverwatchdog.data.logger.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository private constructor(
    private val localData: LocalData,
    private val fileLogger: FileLogger
) {

    suspend fun isRequiredDataSet() = withContext(Dispatchers.IO) {
        localData.serverAddress.isNotBlank()
    }

    suspend fun getServerAddress() = withContext(Dispatchers.IO) {
        localData.serverAddress
    }

    suspend fun saveServerAddress(serverAddress: String) = withContext(Dispatchers.IO) {
        localData.serverAddress = serverAddress
    }

    suspend fun getInterval() = withContext(Dispatchers.IO) {
        localData.interval
    }

    suspend fun saveIntervalInMillis(interval: Long) = withContext(Dispatchers.IO) {
        localData.interval = interval
    }

    suspend fun canWatchOnlyOverWifi(): Boolean = withContext(Dispatchers.IO) {
        localData.canWatchOnlyOverWifi
    }

    suspend fun enableCanWatchOnlyOverWifi(enabled: Boolean) = withContext(Dispatchers.IO) {
        localData.canWatchOnlyOverWifi = enabled
    }

    suspend fun shouldWatchdogRun(): Boolean = withContext(Dispatchers.IO) {
        localData.shouldWatchdogRun
    }

    suspend fun enableWatchdog(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        localData.shouldWatchdogRun = isEnabled
    }

    suspend fun getLastCheckupData(): String = fileLogger.getLastCheckupData()

    suspend fun getFullLogs(): List<String> = fileLogger.getFullLogs()

    companion object {
        private var instance: Repository? = null
        fun getInstance() =
            instance ?: Repository(
                WatchdogSharedPreferences.getInstance(WatchdogApplication.appContext),
                fileLogger = FileLogger.getInstance()
            ).also {
                instance = it
            }
    }
}
