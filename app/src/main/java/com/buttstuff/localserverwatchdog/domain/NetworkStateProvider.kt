package com.buttstuff.localserverwatchdog.domain

import android.content.Context
import android.net.wifi.WifiManager
import com.buttstuff.localserverwatchdog.WatchdogApplication

class NetworkStateProvider private constructor(private val context: Context) {

    private val wifiManager: WifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun isWifiConnected() = wifiManager.isWifiEnabled

    companion object {
        private var instance: NetworkStateProvider? = null
        fun getInstance() = instance ?: NetworkStateProvider(WatchdogApplication.appContext).also { instance = it }
    }
}
