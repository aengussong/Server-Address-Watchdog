package com.buttstuff.localserverwatchdog.ui.screen

import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buttstuff.localserverwatchdog.ui.navigation.OnBoarding
import com.buttstuff.localserverwatchdog.ui.screen.set_ip.SetServerAddressScreen

@Composable
fun WatchdogOnboardingScreen(onOnboardingFinished: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = OnBoarding.SetScreenAddress.route) {
        composable(OnBoarding.SetScreenAddress.route) {
            SetServerAddressScreen {
                navController.navigate(OnBoarding.SetInterval.route)
            }
        }
        composable(OnBoarding.SetInterval.route, enterTransition = {
            slideInHorizontally()
        }) {
            SetIntervalScreen {
                onOnboardingFinished()
            }
        }
    }
}
