package com.buttstuff.localserverwatchdog.domain

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.data.logger.FileLogger
import com.buttstuff.localserverwatchdog.inexact_background_work.InexactBackgroundWorkManager
import com.buttstuff.localserverwatchdog.ui.WatchdogReceiver
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_UP

private const val WATCHDOG_CHANNEL = "watchdog_notification"

class WatchdogManager private constructor(
    private val context: Context,
    private val watchdogReachabilityManager: WatchdogReachabilityManager,
    private val repository: Repository,
    private val logger: FileLogger,
    private val inexactBackgroundWorkManager: InexactBackgroundWorkManager
) {
    /**
     * @return - true if assigned server is running
     * */
    suspend fun checkServerOnce(): Boolean {
        return watchdogReachabilityManager.checkUserServerStatus().isServerOn
    }

    fun isWatchdogRunning(): Boolean {
        return getActionIntentWithFlags(PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null
    }

    fun stopWatchdog() {
        val alarmManager = getAlarmManager()
        // it's important to cancel both alarm manager and pending intent in order to be able to detect whether
        // watchdog was stopped
        alarmManager.cancel(getActionIntent())
        getActionIntent().cancel()

        inexactBackgroundWorkManager.cancelFailSafe()
    }

    suspend fun startWatchdog() {
        val alarmManager = getAlarmManager()
        val actionIntent = getActionIntent()

        val interval = repository.getInterval()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, actionIntent
        )

        inexactBackgroundWorkManager.scheduleFailSafe()
    }

    suspend fun checkServerAndScheduleNextCheckup() {
        // reschedule checkup
        getAlarmManager().cancel(getActionIntent())
        startWatchdog()

        val wasLastCheckupSuccessful = wasLastCheckupSuccessful()
        val checkStatus = watchdogReachabilityManager.checkUserServerStatus()

        val didStatusChange = wasLastCheckupSuccessful != checkStatus.isServerOn
        if (didStatusChange) {
            sendNotification(checkStatus, repository.getServerAddress())
        }
    }

    private fun sendNotification(checkStatus: ServerCheckResult, serverAddress: String) {
        createNotificationChannel()
        val message = when (checkStatus) {
            is ServerCheckResult.ServerOn -> context.getString(R.string.notif_content_server_on, serverAddress)
            is ServerCheckResult.ServerOff -> context.getString(R.string.notif_content_server_down, serverAddress)
            is ServerCheckResult.FailedWifiRestriction -> context.getString(R.string.notif_content_wifi_restriction)
            is ServerCheckResult.NetworkDown -> context.getString(R.string.notif_content_network_down)
        }
        val title = context.getString(R.string.notif_title, serverAddress)
        val notification = NotificationCompat.Builder(context, WATCHDOG_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        val id = System.currentTimeMillis().hashCode()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            logger.logException(e)
        }
    }

    private suspend fun wasLastCheckupSuccessful(): Boolean? {
        return logger.getLastCheckupData().takeIf { it.isNotBlank() }?.contains(SERVER_STATUS_UP)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = WATCHDOG_CHANNEL
            val descriptionText = "I'm just a watchdog"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(WATCHDOG_CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getAlarmManager() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * @see getActionIntent
     * */
    private fun getActionIntentWithFlags(flags: Int): PendingIntent? {
        val intent = Intent(context, WatchdogReceiver::class.java)
        return PendingIntent.getBroadcast(context, 101, intent, flags)
    }

    // documentation states that null will be returned only if we'll pass PendingIntent.FLAG_NO_CREATE, so it is safe
    // to use not-null assertion operator
    // TLDR this shouldn't throw error ever
    private fun getActionIntent(): PendingIntent = getActionIntentWithFlags(PendingIntent.FLAG_IMMUTABLE)!!


    companion object {
        private var instance: WatchdogManager? = null
        fun getInstance() = instance ?: WatchdogManager(
            context = WatchdogApplication.appContext,
            watchdogReachabilityManager = WatchdogReachabilityManager.getInstance(),
            repository = Repository.getInstance(),
            logger = FileLogger.getInstance(),
            inexactBackgroundWorkManager = InexactBackgroundWorkManager.getInstance()
        ).also { instance = it }
    }
}