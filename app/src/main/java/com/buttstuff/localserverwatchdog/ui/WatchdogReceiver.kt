package com.buttstuff.localserverwatchdog.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WatchdogReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("WATCHDOG: received emit")
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WatchdogManager.getInstance().checkServerAndScheduleNextCheckup()
            } catch (ex: Exception) {
                println("WATCHDOG: got error : ${ex.message}")
            } finally {
                println("WATCHDOG: finished broadcast action")
            }
            result.finish()
        }
    }
}