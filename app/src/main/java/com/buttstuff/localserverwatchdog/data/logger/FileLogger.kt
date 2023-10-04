package com.buttstuff.localserverwatchdog.data.logger

import android.content.Context
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.WatchdogApplication
import com.buttstuff.localserverwatchdog.domain.ServerCheckResult
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
private const val LOG_FILE_DATE_PATTERN = "yyyy-MM-dd"
private const val LOG_FOLDER_NAME = "watchdog_logs"
private const val LOG_FILE_NAME_POSTFIX = "watchdog_log.txt"

private const val MAX_LOG_LIFESPAN_IN_DAYS = 7

/**
 * Main Logger implementation. All logs operations that are meant to be logged in the file should be logged throuh
 * [log] function or formatted with [logLineTimeFormatter] in order to ensure one log style and ease parsing and uniform
 * logs handling.
 * */
class FileLogger private constructor(private val context: Context) : Logger {
    private val logStringWifiOff: String by lazy {
        context.getString(R.string.error_wifi_is_off)
    }
    private val logStringNetworkDown: String by lazy {
        context.getString(R.string.error_network_unavailable)
    }
    private val logLineTimeFormatter = SimpleDateFormat(LOG_LINE_TIME_PATTERN, Locale.getDefault())
    private val logFileNameFormatter = SimpleDateFormat(LOG_FILE_DATE_PATTERN, Locale.getDefault())

    private val mutex = Mutex()

    private val writeChannel = Channel<String>()

    private val logFolder: File
        get() = File(context.filesDir, LOG_FOLDER_NAME).also { directory ->
            if (!directory.exists()) {
                directory.mkdirs()
            }
        }

    private val logFiles: Array<out File>
        get() = logFolder.listFiles().orEmpty()

    private val currentLogFile: File
        get() {
            val fileName = createLogFileName()
            return File(logFolder, fileName)
        }

    init {
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

    override fun logCheckup(serverAddress: String, checkupResult: ServerCheckResult) {
        val status = when (checkupResult) {
            is ServerCheckResult.ServerOn -> "$serverAddress $SERVER_STATUS_UP"
            is ServerCheckResult.ServerOff -> "$serverAddress $SERVER_STATUS_DOWN"
            is ServerCheckResult.FailedWifiRestriction -> "$ERROR_POINTER $logStringWifiOff"
            is ServerCheckResult.NetworkDown -> "$ERROR_POINTER $logStringNetworkDown"
        }
        log(status)
    }

    override fun logException(exception: Throwable) {
        Firebase.crashlytics.recordException(exception)
    }

    // todo should refactor this function to return not only String, but String and Status, so the logic of parsing
    //  written logs and logging would be located in the same class
    suspend fun getLastCheckupData(): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            logFiles.apply { sortByDescending { it.lastModified() } }
                .asSequence()
                .map {
                    var line: String? = null
                    BufferedReader(FileReader(it)).use { input ->
                        while (true) {
                            line = input.readLine() ?: break
                        }
                    }
                    line
                }.filter { !it.isNullOrBlank() }
                .firstOrNull() ?: ""
        }
    }

    suspend fun getFullLogs(): List<String> = withContext(Dispatchers.IO) {
        val lines = mutableListOf<String>()

        mutex.withLock {
            logFiles.apply { sortBy { it.lastModified() } }.forEach { logFile ->
                BufferedReader(FileReader(logFile)).use { input ->
                    while (true) {
                        val line = input.readLine() ?: break
                        lines.add(line)
                    }
                }
            }
        }

        lines
    }

    suspend fun clearOutdatedData() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val dayInMillis = TimeUnit.DAYS.toMillis(1)
            val relevantLogNames = List(MAX_LOG_LIFESPAN_IN_DAYS) { index ->
                val date = Date(System.currentTimeMillis() - dayInMillis * index)
                createLogFileName(date)
            }

            logFiles.filter { !relevantLogNames.contains(it.name) }.forEach(::deleteFile)
        }
    }

    private fun createLogFileName(date: Date = Date()): String {
        val currentDate = logFileNameFormatter.format(date)
        return "${currentDate}_$LOG_FILE_NAME_POSTFIX"
    }

    private fun formatLog(data: String): String {
        return "${logLineTimeFormatter.format(Date())} $data\n"
    }

    private fun writeToFile(data: String) {
        try {
            currentLogFile.createNewFile()
            FileOutputStream(currentLogFile, true).use { outputStream ->
                OutputStreamWriter(outputStream).use { it.append(data) }
            }
        } catch (e: IOException) {
            Firebase.crashlytics.recordException(e)
            println("Failed to write log: ${e.message}")
        }
    }

    private fun deleteFile(file: File) {
        var isDeleted = false
        try {
            isDeleted = file.delete()
            if (!isDeleted) {
                file.name
                    .takeIf { it.isNotEmpty() }
                    ?.let { fileName -> isDeleted = context.deleteFile(fileName) }
                    ?: println("File couldn't be deleted (missing file name): ${file.absolutePath}")
            }
        } catch (throwable: Throwable) {
            logException(throwable)
        }

        if (!isDeleted) {
            println("Couldn't delete a file: ${file.absolutePath}")
        }
    }

    companion object {
        private var instance: FileLogger? = null
        fun getInstance() = instance ?: FileLogger(WatchdogApplication.appContext).also {
            instance = it
        }
    }
}
