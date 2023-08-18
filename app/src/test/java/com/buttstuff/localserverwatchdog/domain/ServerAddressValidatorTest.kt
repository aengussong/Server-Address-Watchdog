package com.buttstuff.localserverwatchdog.domain

import com.buttstuff.localserverwatchdog.util.ResultObject
import com.buttstuff.localserverwatchdog.util.assertIsInstance
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ServerAddressValidatorTest {

    private val validator = ServerAddressValidator()

    @Test
    fun `valid ip - should return success`() = runBlocking {
        val ip = "142.251.40.174"
        val result = validator.validate(ip)
        assertIsInstance<ResultObject.Success>(result)
    }

    @Test
    fun `invalid ip - should return validation error`() = runBlocking {
        val ip = "1.x.8.-1"
        val result = validator.validate(ip)
        assertIsInstance<ResultObject.Error>(result)
    }

    @Test
    fun `valid unreachable ip - should return unreachable error`() = runBlocking {
        val ip = "192.168.0.34"
        val result = validator.validate(ip)
        assertIsInstance<ResultObject.Error>(result)
    }

    @Test
    fun `valid host - should return success`() = runBlocking {
        val host = "www.google.com"
        val result = validator.validate(host)
        assertIsInstance<ResultObject.Success>(result)
    }

    @Test
    fun `invalid host - should return validation error`() = runBlocking {
        val host = ";alkdjfl"
        val result = validator.validate(host)
        assertIsInstance<ResultObject.Error>(result)
    }

    @Test
    fun `valid unreachable host - should return unreachable error`() = runBlocking {
        val host = "ijustmadeupthishost.com"
        val result = validator.validate(host)
        assertIsInstance<ResultObject.Error>(result)
    }

}
