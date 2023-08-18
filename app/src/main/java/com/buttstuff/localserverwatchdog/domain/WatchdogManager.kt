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
import com.buttstuff.localserverwatchdog.data.logger.Logger
import com.buttstuff.localserverwatchdog.ui.WatchdogReceiver

private const val WATCHDOG_CHANNEL = "watchdog_notification"

class WatchdogManager private constructor(
    private val context: Context,
    private val apiChecker: ApiChecker,
    private val repository: Repository,
    private val logger: Logger
) {
    /**
     * @return - true if assigned server is running
     * */
    suspend fun checkServerOnce(): Boolean = apiChecker.isWorking()

    fun stopWatchdog() {
        val alarmManager = getAlarmManager()
        alarmManager.cancel(getActionIntent())
    }

    fun isWatchdogRunning(): Boolean {
        TODO("Not implemented")
    }

    suspend fun startWatchdog() {
        val alarmManager = getAlarmManager()
        val actionIntent = getActionIntent()

        val interval = repository.getInterval()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            actionIntent
        )
    }

    suspend fun checkServerAndScheduleNextCheckup() {
        // reschedule checkup
        getAlarmManager().cancel(getActionIntent())
        startWatchdog()

        val result = apiChecker.isWorking()
        sendNotification(result)
    }

    private fun sendNotification(isServerWorking: Boolean) {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(context, WATCHDOG_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Watchdog result")
            .setContentText("Is server running: $isServerWorking")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val id = System.currentTimeMillis().hashCode()
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (e: SecurityException) {
            logger.logException(e)
        }
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
    private fun getActionIntent(): PendingIntent {
        val intent = Intent(context, WatchdogReceiver::class.java)
        return PendingIntent.getBroadcast(context, 101, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private var instance: WatchdogManager? = null
        fun getInstance() = instance ?: WatchdogManager(
            context = WatchdogApplication.appContext,
            apiChecker = ApiChecker.getInstance(),
            repository = Repository.getInstance(),
            logger = FileLogger.getInstance()
        ).also { instance = it }
    }
}