package com.buttstuff.localserverwatchdog.inexact_background_work.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.data.logger.FileLogger
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import java.util.concurrent.TimeUnit

class FailsafeWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val watchdogManager = WatchdogManager.getInstance()
        val repository = Repository.getInstance()
        val shouldWatchdogRun = repository.shouldWatchdogRun() && repository.isRequiredDataSet()
        val shouldReviveWatchdog = !watchdogManager.isWatchdogRunning() && shouldWatchdogRun

        if (shouldReviveWatchdog) {
            watchdogManager.startWatchdog()
            FileLogger.getInstance().logException(IllegalStateException("Had to revive watchdog."))
        }

        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "failsafe_worker"
    }
}

val failsafeRequest: PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<FailsafeWorker>(1L, TimeUnit.HOURS)
        .build()
