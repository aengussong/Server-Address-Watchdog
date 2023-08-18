package com.buttstuff.localserverwatchdog.ui.navigation

import androidx.compose.ui.graphics.painter.Painter

interface NavigationScreen {
    val route: String
}

class BottomNavigationScreen(screen: NavigationScreen, val icon: Painter) : NavigationScreen by screen
