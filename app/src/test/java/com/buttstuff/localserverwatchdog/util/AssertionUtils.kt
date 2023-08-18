package com.buttstuff.localserverwatchdog.util

inline fun <reified T> assertIsInstance(instance: Any) {
    if (instance is T) return

    throw AssertionError("Expected ${T::class.simpleName} but got ${instance::class.simpleName}:$instance")
}
