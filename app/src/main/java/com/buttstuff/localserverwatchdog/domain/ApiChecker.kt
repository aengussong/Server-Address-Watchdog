package com.buttstuff.localserverwatchdog.domain

import com.buttstuff.localserverwatchdog.data.Repository
import com.buttstuff.localserverwatchdog.data.logger.FileLogger
import com.buttstuff.localserverwatchdog.data.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

private const val CHECKUP_TIMEOUT = 5000

class ApiChecker private constructor(private val logger: Logger, private val repository: Repository) {

    suspend fun isWorking(): Boolean {
        return withContext(Dispatchers.IO) {
            val address = repository.getServerAddress()
            val status = InetAddress.getByName(address).isReachable(CHECKUP_TIMEOUT)
            logger.logCheckup(address, status)
            status
        }
    }

    companion object {
        fun getInstance() = ApiChecker(FileLogger.getInstance(), Repository.getInstance())
    }
}