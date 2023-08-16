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
import com.buttstuff.localserverwatchdog.ui.WatchdogReceiver
import java.util.concurrent.TimeUnit

private const val WATCHDOG_CHANNEL = "watchdog_notification"
private const val WATCHDOG_INTERVAL: Long = 5L

class WatchdogManager private constructor() {

    private val apiChecker = ApiChecker()

    fun startWatchdog(context: Context) {
        val alarmManager = getAlarmManager(context)
        val actionIntent = getActionIntent(context)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(WATCHDOG_INTERVAL),
            actionIntent
        )
    }

    fun checkServer(context: Context) {
        val am = getAlarmManager(context)
        am.cancel(getActionIntent(context))
        startWatchdog(context)
        checkApi(context)
    }

    private fun checkApi(context: Context) {
        val result = apiChecker.isWorking()
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, WATCHDOG_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Watchdog result")
            .setContentText("Is server running: $result")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val id = System.currentTimeMillis().hashCode()
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    //todo create new channel before app release in order to adjust importance and priority
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = WATCHDOG_CHANNEL
            val descriptionText = "I'm just a watchdog"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(WATCHDOG_CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getAlarmManager(context: Context) = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private fun getActionIntent(context: Context): PendingIntent {
        val intent = Intent(context, WatchdogReceiver::class.java)
        return PendingIntent.getBroadcast(context, 101, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private var instance: WatchdogManager? = null
        fun getInstance() = instance ?: WatchdogManager().also { instance = it }
    }
}