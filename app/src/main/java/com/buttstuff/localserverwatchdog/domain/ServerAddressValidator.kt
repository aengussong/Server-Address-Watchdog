package com.buttstuff.localserverwatchdog.domain

import androidx.annotation.StringRes
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.util.ResultObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ServerAddressValidator {

    suspend fun validate(serverAddress: String): ResultObject = withContext(Dispatchers.IO) {
        delay(3_000)
        ServerAddressValidationError.AddressInvalid.toResultObject()
    }

    private suspend fun isIpValid(serverAddress: String): ResultObject {
        return ResultObject.Success
    }

    private suspend fun isHostValid(serverAddress: String): ResultObject {
        return ResultObject.Success
    }

    private suspend fun isReachable(serverAddress: String): ResultObject {
        return ResultObject.Success
    }
}

sealed class ServerAddressValidationError(@StringRes val text: Int) {
    object AddressInvalid: ServerAddressValidationError(R.string.error_server_address_invalid)
    object AddressUnreachable: ServerAddressValidationError(R.string.error_server_address_unreachable)

    fun toResultObject() = ResultObject.Error(this.text)
}