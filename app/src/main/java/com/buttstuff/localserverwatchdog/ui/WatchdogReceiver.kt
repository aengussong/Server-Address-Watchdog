package com.buttstuff.localserverwatchdog.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WatchdogReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WatchdogManager.getInstance().checkServerAndScheduleNextCheckup()
            } catch (ex: Exception) {
                Firebase.crashlytics.recordException(ex)
                WatchdogManager.getInstance().startWatchdog()
            }
            result.finish()
        }
    }
}