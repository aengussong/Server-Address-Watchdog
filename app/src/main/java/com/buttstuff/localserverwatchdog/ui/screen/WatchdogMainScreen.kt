package com.buttstuff.localserverwatchdog.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.ui.navigation.BottomNavigationScreen
import com.buttstuff.localserverwatchdog.ui.navigation.Main
import com.buttstuff.localserverwatchdog.ui.navigation.OnBoarding
import com.buttstuff.localserverwatchdog.ui.screen.logs.LogsScreen
import com.buttstuff.localserverwatchdog.ui.screen.set_interval.SetIntervalScreen
import com.buttstuff.localserverwatchdog.ui.screen.set_ip.SetServerAddressScreen
import com.buttstuff.localserverwatchdog.ui.screen.settings.SettingsScreen
import com.buttstuff.localserverwatchdog.ui.screen.test_server.TestServerScreen
import com.buttstuff.localserverwatchdog.ui.theme.LocalServerWatchdogTheme

@Composable
private fun bottomNavigationEntries() = listOf(
    BottomNavigationScreen(Main.TestServer, painterResource(R.drawable.ic_dog_small)),
    BottomNavigationScreen(Main.Settings, rememberVectorPainter(Icons.Filled.Settings)),
    BottomNavigationScreen(Main.Logs, rememberVectorPainter(Icons.Sharp.Email))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchdogMainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        NavigationBar {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            bottomNavigationEntries().forEach { screen ->
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    icon = { Icon(screen.icon, contentDescription = null) },
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }

                    })
            }
        }
    }) { innerPadding ->
        NavHost(navController, startDestination = Main.TestServer.route, Modifier.padding(innerPadding)) {
            composable(Main.TestServer.route) { TestServerScreen() }
            composable(Main.Settings.route) {
                SettingsScreen(
                    onEditAddress = { navController.navigate(OnBoarding.SetScreenAddress.route) },
                    onEditInterval = { navController.navigate(OnBoarding.SetInterval.route) }
                )
            }
            composable(Main.Logs.route) { LogsScreen() }

            composable(OnBoarding.SetInterval.route) {
                SetIntervalScreen { navController.navigate(Main.Settings.route) }
            }
            composable(OnBoarding.SetScreenAddress.route) {
                SetServerAddressScreen { navController.navigate(Main.Settings.route) }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LocalServerWatchdogTheme {
        WatchdogMainScreen()
    }
}
