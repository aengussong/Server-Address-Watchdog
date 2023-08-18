package com.buttstuff.localserverwatchdog.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

private const val PREF_NAME = "watchdog_pref"

private const val KEY_SERVER_ADDRESS = "key_server_address"
private const val KEY_WATCHDOG_INTERVAL = "key_interval"

//todo migrate from preferences to datastore
class WatchdogSharedPreferences private constructor(private val preferences: SharedPreferences) : LocalData {

    override var serverAddress: String
        get() = preferences.getString(KEY_SERVER_ADDRESS, null) ?: ""
        set(value) = preferences.edit {
            putString(KEY_SERVER_ADDRESS, value)
        }
    override var interval: Long
        get() = preferences.getLong(KEY_WATCHDOG_INTERVAL, TimeUnit.HOURS.toMillis(1))
        set(value) = preferences.edit {
            putLong(KEY_WATCHDOG_INTERVAL, value)
        }

    companion object {
        private var instance: LocalData? = null
        fun getInstance(context: Context): LocalData =
            instance ?: WatchdogSharedPreferences(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)).also {
                instance = it
            }
    }
}

interface LocalData {
    var serverAddress: String
    var interval: Long
}
