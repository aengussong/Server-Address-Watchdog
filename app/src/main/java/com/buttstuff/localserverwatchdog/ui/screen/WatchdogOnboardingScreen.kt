package com.buttstuff.localserverwatchdog.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buttstuff.localserverwatchdog.ui.navigation.OnBoarding

@Composable
fun WatchdogOnboardingScreen(onOnboardingFinished: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = OnBoarding.SetIp.route) {
        composable(OnBoarding.SetIp.route) {
            SetIpScreen {
                navController.navigate(OnBoarding.SetInterval.route)
            }
        }
        composable(OnBoarding.SetInterval.route) {
            SetIntervalScreen {
                onOnboardingFinished()
            }
        }
    }
}
