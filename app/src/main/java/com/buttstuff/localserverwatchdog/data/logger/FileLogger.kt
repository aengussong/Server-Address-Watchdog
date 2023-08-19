package com.buttstuff.localserverwatchdog.data.logger

import android.content.Context
import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_DOWN
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_UP
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
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

private const val MAX_LOG_LIFESPAN_IN_DAYS = 7

class FileLogger private constructor(private val context: Context) : Logger {
    private val logDateExample: String by lazy { logLineTimeFormatter.format(Date()) }

    private val logLineTimeFormatter = SimpleDateFormat(LOG_LINE_TIME_PATTERN, Locale.getDefault())

    private val logFile: File
        get() = File(context.filesDir, LOG_FILE_NAME)

    override fun log(data: String) {
        GlobalScope.launch(Dispatchers.IO) {
            writeToFile(data)
        }
    }

    override fun logCheckup(serverAddress: String, isAvailable: Boolean) {
        val status = if (isAvailable) SERVER_STATUS_UP else SERVER_STATUS_DOWN
        val data = "${logLineTimeFormatter.format(Date())} $serverAddress $status"
        log(data)
    }

    override fun logException(exception: Throwable) {
        Firebase.crashlytics.recordException(exception)
    }

    suspend fun getLastCheckupData(): String = withContext(Dispatchers.IO) {
        var line = ""

        BufferedReader(FileReader(logFile)).use { input ->
            while (true) {
                line = input.readLine() ?: break
            }
        }

        line
    }

    suspend fun getFullLogs(): List<String> = withContext(Dispatchers.IO) {
        val lines = mutableListOf<String>()

        BufferedReader(FileReader(logFile)).use { input ->
            while (true) {
                val line = input.readLine() ?: break
                lines.add(line)
            }
        }

        lines
    }

    suspend fun clearOutdatedData() = withContext(Dispatchers.IO) {
        val validatedFileContent = mutableListOf<String>()
        BufferedReader(FileReader(logFile)).use { reader ->
            while (true) {
                val log = reader.readLine() ?: break
                val testDate = log.subSequence(0, logDateExample.length).toString()
                val date = logLineTimeFormatter.parse(testDate) ?: Date()
                val diffDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - date.time)
                if (diffDays <= MAX_LOG_LIFESPAN_IN_DAYS) {
                    validatedFileContent.add(log)
                }
            }
        }

        try {
            logFile.delete()
        } catch (e: SecurityException) {
            Firebase.crashlytics.recordException(e)
        }

        validatedFileContent.forEach { data ->
            log(data)
        }
    }

    private fun writeToFile(data: String) {
        try {
            logFile.createNewFile()
            FileOutputStream(logFile, true).use { outputStream ->
                OutputStreamWriter(outputStream).use { it.append(data + "\n") }
                outputStream.flush()
            }
        } catch (e: IOException) {
            Firebase.crashlytics.recordException(e)
            println("Failed to write log: ${e.message}")
        }
    }

    companion object {
        private var instance: FileLogger? = null
        fun getInstance() = instance ?: FileLogger(WatchdogApplication.appContext)?.also {
            instance = it
        }
    }
}
