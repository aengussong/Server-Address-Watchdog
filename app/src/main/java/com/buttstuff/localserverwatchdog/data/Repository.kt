package com.buttstuff.localserverwatchdog.data

import kotlinx.coroutines.delay

class Repository private constructor() {

    suspend fun isRequiredDataSet() = false

    suspend fun saveServerAddress(serverAddress: String) {
        delay(1000)
    }

    companion object {
        private var instance: Repository? = null
        fun getInstance() = instance ?: Repository().also { instance = it }
    }
}
