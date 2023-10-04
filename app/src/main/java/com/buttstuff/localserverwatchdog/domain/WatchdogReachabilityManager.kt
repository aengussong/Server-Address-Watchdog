package com.buttstuff.localserverwatchdog.domain

import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.data.logger.FileLogger

class WatchdogReachabilityManager private constructor(
    private val logger: FileLogger,
    private val repository: Repository,
    private val networkStateProvider: NetworkStateProvider,
    private val serverReachabilityChecker: ServerReachabilityChecker
) {

    suspend fun checkUserServerStatus(): ServerCheckResult {
        val userAddress = repository.getServerAddress()
        return checkStatus(userAddress).also { checkResult ->
            logger.logCheckup(userAddress, checkResult)
        }
    }

    private suspend fun checkStatus(userAddress: String): ServerCheckResult {
        if (!isPassingWifiRestriction()) {
            return ServerCheckResult.FailedWifiRestriction
        }

        val isUserAddressReachable = serverReachabilityChecker.canReach(userAddress)
        val isNetworkAvailable = isUserAddressReachable || networkStateProvider.isNetworkReachable()

        return when {
            isUserAddressReachable -> ServerCheckResult.ServerOn
            !isUserAddressReachable && isNetworkAvailable -> ServerCheckResult.ServerOff
            else -> ServerCheckResult.NetworkDown
        }
    }

    private suspend fun isPassingWifiRestriction() =
        !repository.canWatchOnlyOverWifi() || (repository.canWatchOnlyOverWifi() && networkStateProvider.isWifiConnected())

    companion object {
        private var instance: WatchdogReachabilityManager? = null
        fun getInstance() = instance ?: WatchdogReachabilityManager(
            FileLogger.getInstance(),
            Repository.getInstance(),
            NetworkStateProvider.getInstance(),
            ServerReachabilityChecker.getInstance()
        ).also {
            instance = it
        }
    }
}

sealed interface ServerCheckResult {
    data object ServerOn : ServerCheckResult
    data object ServerOff : ServerCheckResult
    data object NetworkDown : ServerCheckResult
    data object FailedWifiRestriction: ServerCheckResult

    val isServerOn: Boolean get() =  this is ServerOn
}
