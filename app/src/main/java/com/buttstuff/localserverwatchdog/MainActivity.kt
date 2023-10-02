package com.buttstuff.localserverwatchdog

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buttstuff.localserverwatchdog.ui.navigation.Main
import com.buttstuff.localserverwatchdog.ui.navigation.OnBoarding
import com.buttstuff.localserverwatchdog.ui.screen.WatchdogMainScreen
import com.buttstuff.localserverwatchdog.ui.screen.WatchdogOnboardingScreen
import com.buttstuff.localserverwatchdog.ui.theme.LocalServerWatchdogTheme
import com.buttstuff.localserverwatchdog.ui.viewmodel.WatchdogViewModel

class MainActivity : ComponentActivity() {
    private val permissionHandler =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            LocalServerWatchdogTheme {
                LocalServerWatchdog()
            }
        }

        handlePermissions()
    }

    private fun handlePermissions() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        if (permissions.isNotEmpty()) {
            permissionHandler.launch(permissions.toTypedArray())
        }
    }
}

@Composable
fun LocalServerWatchdog(watchdogViewModel: WatchdogViewModel = viewModel()) {
    val navController = rememberNavController()
    val hasRequiredData by watchdogViewModel.hasRequiredData.collectAsState(initial = true)
    NavHost(navController, startDestination = Main().route) {
        composable(OnBoarding().route) {
            WatchdogOnboardingScreen {
                navController.navigate(Main().route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
        composable(Main().route) { WatchdogMainScreen() }
    }

    if (!hasRequiredData) {
        navController.navigate(OnBoarding().route) {
            popUpTo(navController.graph.id)
        }
    }
}