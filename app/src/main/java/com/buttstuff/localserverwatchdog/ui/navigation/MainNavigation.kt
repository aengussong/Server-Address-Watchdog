package com.buttstuff.localserverwatchdog.ui.navigation

//todo in order to get route I have to instantiate class - not good, fix it
open class Main(override val route: String = "Main") : NavigationScreen {
    object TestServer : Main("testserver")
    object Settings : Main("settings")
    object Logs : Main("logging")
}
