package com.buttstuff.localserverwatchdog.domain

import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.util.ResultObject
import com.buttstuff.localserverwatchdog.util.isSuccess
import com.buttstuff.localserverwatchdog.util.or
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

private const val REACHABILITY_CHECK_TIMEOUT_MS = 2000

class ServerAddressValidator {

    suspend fun validate(serverAddress: String): ResultObject = withContext(Dispatchers.IO) {
        var validationResult: ResultObject = isIpV4Valid(serverAddress) or { isHostValid(serverAddress) }
        if (validationResult.isSuccess()) {
            validationResult = isReachable(serverAddress)
        }
        validationResult
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

    private fun isReachable(serverAddress: String): ResultObject {
        val isReachable = try {
            Socket().use {
                val socketAddress = InetSocketAddress(serverAddress, 80)
                it.connect(socketAddress, REACHABILITY_CHECK_TIMEOUT_MS)
                true
            }
        } catch (e: Throwable) {
            false
        }
        if (isReachable) return ResultObject.Success
        return ServerAddressValidationError.AddressUnreachable.toResultObject()
    }
}

sealed class ServerAddressValidationError(@StringRes val text: Int) {
    object AddressInvalid : ServerAddressValidationError(R.string.error_server_address_invalid)
    object AddressUnreachable : ServerAddressValidationError(R.string.error_server_address_unreachable)

    fun toResultObject() = ResultObject.Error(this.text)
}