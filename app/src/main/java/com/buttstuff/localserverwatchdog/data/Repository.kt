package com.buttstuff.localserverwatchdog.data

class Repository private constructor(){

    suspend fun isRequiredDataSet() = false

    companion object {
        private var instance: Repository? = null
        fun getInstance() = instance ?: Repository().also { instance = it }
    }
}
