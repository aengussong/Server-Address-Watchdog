package com.buttstuff.localserverwatchdog.data.logger

import android.content.Context
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.util.ERROR_POINTER
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_DOWN
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_UP
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val LOG_LINE_TIME_PATTERN = "d MMM y HH:mm:ss"
private const val LOG_FILE_NAME = "watchdog_logs.txt"
private const val LOG_FILE_NAME_COPY = "watchdog_copy.txt"

private const val MAX_LOG_LIFESPAN_IN_DAYS = 7

class FileLogger private constructor(private val context: Context) : Logger {
    private val logStringWifiOff: String by lazy {
        context.getString(R.string.error_wifi_is_off)
    }
    private val logDateExample: String by lazy { logLineTimeFormatter.format(Date()) }

    private val logLineTimeFormatter = SimpleDateFormat(LOG_LINE_TIME_PATTERN, Locale.getDefault())

    private val mutex = Mutex()

    private val writeChannel = Channel<String>()
    private val logFile: File
        get() = File(context.filesDir, LOG_FILE_NAME)

    private val logFileTemp: File
        get() = File(context.filesDir, LOG_FILE_NAME_COPY)

    init {
        logFile.createNewFile()
        GlobalScope.launch(Dispatchers.IO) {
            for (logString in writeChannel) {
                writeToFile(logString)
            }
        }
    }

    override fun log(data: String) {
        GlobalScope.launch(Dispatchers.IO) {
            mutex.withLock {
                writeChannel.send(formatLog(data))
            }
        }
    }

    override fun logCheckup(serverAddress: String, isAvailable: Boolean) {
        val status = if (isAvailable) SERVER_STATUS_UP else SERVER_STATUS_DOWN
        log("$serverAddress $status")
    }

    override fun logWifiIsOff() {
        log("$ERROR_POINTER $logStringWifiOff")
    }

    override fun logException(exception: Throwable) {
        Firebase.crashlytics.recordException(exception)
    }

    suspend fun getLastCheckupData(): String = withContext(Dispatchers.IO) {
        var line = ""

        mutex.withLock {
            BufferedReader(FileReader(logFile)).use { input ->
                while (true) {
                    line = input.readLine() ?: break
                }
            }
        }

        line
    }

    suspend fun getFullLogs(): List<String> = withContext(Dispatchers.IO) {
        val lines = mutableListOf<String>()

        mutex.withLock {
            BufferedReader(FileReader(logFile)).use { input ->
                while (true) {
                    val line = input.readLine() ?: break
                    lines.add(line)
                }
            }
        }

        lines
    }

    suspend fun clearOutdatedData() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val output = FileOutputStream(File(context.filesDir, LOG_FILE_NAME_COPY))
            val writer = BufferedWriter(OutputStreamWriter(output))
            BufferedReader(FileReader(logFile)).use { reader ->
                while (true) {
                    val log = reader.readLine() ?: break
                    val testDate = log.subSequence(0, logDateExample.length).toString()
                    val date = logLineTimeFormatter.parse(testDate) ?: Date()
                    val diffDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - date.time)
                    if (diffDays <= MAX_LOG_LIFESPAN_IN_DAYS) {
                        writer.append(log + "\n")
                    }
                }
            }
            writer.close()
            output.close()
        }

        mutex.withLock {
            try {
                logFile.delete()
                logFileTemp.renameTo(logFile)
                logFileTemp.delete()
            } catch (e: SecurityException) {
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    private fun formatLog(data: String): String {
        return "${logLineTimeFormatter.format(Date())} $data\n"
    }

    private fun writeToFile(data: String) {
        try {
            logFile.createNewFile()
            FileOutputStream(logFile, true).use { outputStream ->
                OutputStreamWriter(outputStream).use { it.append(data) }
            }
        } catch (e: IOException) {
            Firebase.crashlytics.recordException(e)
            println("Failed to write log: ${e.message}")
        }
    }

    companion object {
        private var instance: FileLogger? = null
        fun getInstance() = instance ?: FileLogger(WatchdogApplication.appContext).also {
            instance = it
        }
    }
}
