package com.buttstuff.localserverwatchdog.data.logger

interface Logger {
    fun log(data: String)

    fun logException(exception: Throwable)

    fun logCheckup(serverAddress: String, isAvailable: Boolean)
}
