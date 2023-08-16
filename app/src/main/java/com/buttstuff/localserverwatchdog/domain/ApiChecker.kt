package com.buttstuff.localserverwatchdog.domain

import java.net.InetAddress

class ApiChecker {
    fun isWorking() = InetAddress.getByName("192.168.0.233").isReachable(5000)
}