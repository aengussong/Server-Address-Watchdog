package com.buttstuff.localserverwatchdog.domain

import android.content.Context
import android.net.wifi.WifiManager
import com.buttstuff.localserverwatchdog.WatchdogApplication

private const val REFERENCE_ADDRESS = "google.com"

class NetworkStateProvider private constructor(
    private val context: Context,
    private val serverReachabilityChecker: ServerReachabilityChecker
) {

    private val wifiManager: WifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun isWifiConnected() = wifiManager.isWifiEnabled

    suspend fun isNetworkReachable(): Boolean {
        return serverReachabilityChecker.canReach(REFERENCE_ADDRESS)
    }

    companion object {
        private var instance: NetworkStateProvider? = null
        fun getInstance() = instance ?: NetworkStateProvider(
            WatchdogApplication.appContext,
            ServerReachabilityChecker.getInstance()
        ).also { instance = it }
    }
}
