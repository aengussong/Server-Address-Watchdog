package com.buttstuff.localserverwatchdog.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.buttstuff.localserverwatchdog.data.logger.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LogsCleanupWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            FileLogger.getInstance().clearOutdatedData()
        }

        return Result.success()
    }
}

val cleanupLogsRequest: WorkRequest =
    PeriodicWorkRequestBuilder<LogsCleanupWorker>(1L, TimeUnit.DAYS)
        .build()
