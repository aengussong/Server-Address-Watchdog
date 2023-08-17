package com.buttstuff.localserverwatchdog.ui.navigation

//todo in order to get route I have to instantiate class - not good, fix it
open class OnBoarding(override val route: String = "onboarding") : NavigationScreen {
    object SetScreenAddress : OnBoarding("onboarding.setscreenaddress")
    object SetInterval : OnBoarding("onboarding.setinterval")
}
