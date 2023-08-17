package com.buttstuff.localserverwatchdog.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

interface NavigationScreen {
    val route: String
}

class BottomNavigationScreen(screen: NavigationScreen, val icon: ImageVector) : NavigationScreen by screen
