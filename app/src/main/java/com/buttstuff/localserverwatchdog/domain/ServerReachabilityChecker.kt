package com.buttstuff.localserverwatchdog.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

private const val CHECKUP_TIMEOUT = 5000

class ServerReachabilityChecker private constructor() {

    suspend fun canReach(address: String): Boolean {
        return withContext(Dispatchers.IO) {
            InetAddress.getByName(address).isReachable(CHECKUP_TIMEOUT)
        }
    }

    companion object {
        private var instance: ServerReachabilityChecker? = null
        fun getInstance() = instance ?: ServerReachabilityChecker().also {
            instance = it
        }
    }
}
