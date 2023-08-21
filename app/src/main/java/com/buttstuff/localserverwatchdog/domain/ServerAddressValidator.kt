package com.buttstuff.localserverwatchdog.domain

import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.util.ResultObject
import com.buttstuff.localserverwatchdog.util.or
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerAddressValidator {

    suspend fun validate(serverAddress: String): ResultObject = withContext(Dispatchers.IO) {
        isIpV4Valid(serverAddress) or { isHostValid(serverAddress) }
    }

    private fun isIpV4Valid(serverAddress: String): ResultObject {
        //todo checks only IPv4, probably should add IPv6 support
        val isValid = PatternsCompat.IP_ADDRESS.matcher(serverAddress).matches()
        if (isValid) return ResultObject.Success
        return ServerAddressValidationError.AddressInvalid.toResultObject()
    }

    private fun isHostValid(serverAddress: String): ResultObject {
        // this check could be used for both host name and ip, but for now I want to keep them separate, to differentiate
        // between host and ip validation for now
        val isValid = PatternsCompat.DOMAIN_NAME.matcher(serverAddress).matches()
        if (isValid) return ResultObject.Success
        return ServerAddressValidationError.AddressInvalid.toResultObject()
    }
}

sealed class ServerAddressValidationError(@StringRes val text: Int) {
    data object AddressInvalid : ServerAddressValidationError(R.string.error_server_address_invalid)

    fun toResultObject() = ResultObject.Error(this.text)
}