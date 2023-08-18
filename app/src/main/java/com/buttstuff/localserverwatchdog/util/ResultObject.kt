package com.buttstuff.localserverwatchdog.util

import androidx.annotation.StringRes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface ResultObject {
    object Success: ResultObject
    class Error(@StringRes val message: Int): ResultObject
}

// we need to retrieve other instance lazily in order to be able short circuit the expression
inline infix fun ResultObject.or(lazyOther: () -> ResultObject): ResultObject {
    if (this.isSuccess()) return this
    val other = lazyOther()
    if (other.isSuccess()) return other
    return this
}

@OptIn(ExperimentalContracts::class)
fun ResultObject?.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ResultObject.Success)
    }
    return this is ResultObject.Success
}

@OptIn(ExperimentalContracts::class)
fun ResultObject?.isError(): Boolean {
    contract {
        returns(true) implies (this@isError is ResultObject.Error)
    }
    return this is ResultObject.Error
}
