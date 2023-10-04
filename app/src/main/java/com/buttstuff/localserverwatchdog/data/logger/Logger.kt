package com.buttstuff.localserverwatchdog.data.logger

import com.buttstuff.localserverwatchdog.domain.ServerCheckResult

interface Logger {
    fun log(data: String)

    fun logException(exception: Throwable)

    fun logCheckup(serverAddress: String, checkupResult: ServerCheckResult)
}
