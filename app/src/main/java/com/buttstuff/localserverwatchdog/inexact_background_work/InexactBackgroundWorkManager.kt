package com.buttstuff.localserverwatchdog.inexact_background_work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.inexact_background_work.worker.FailsafeWorker
import com.buttstuff.localserverwatchdog.inexact_background_work.worker.cleanupLogsRequest
import com.buttstuff.localserverwatchdog.inexact_background_work.worker.failsafeRequest

class InexactBackgroundWorkManager private constructor(private val context: Context) {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun scheduleCleanUpJob() {
        workManager.enqueue(cleanupLogsRequest)
    }

    fun scheduleFailSafe() {
        workManager.enqueueUniquePeriodicWork(
            FailsafeWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            failsafeRequest
        )
    }

    fun cancelFailSafe() {
        workManager.cancelUniqueWork(FailsafeWorker.UNIQUE_NAME)
    }

    companion object {
        private var instance: InexactBackgroundWorkManager? = null
        fun getInstance() =
            instance ?: InexactBackgroundWorkManager(WatchdogApplication.appContext).also { instance = it }
    }
}
