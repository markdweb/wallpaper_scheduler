package com.cheese.wallpaperscheduler.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home),
    WALLPAPERS("wallpapers", "Wallpapers", Icons.Filled.Wallpaper),
    SCHEDULE("schedule", "Schedule", Icons.Filled.Schedule),
    SETTINGS("settings", "Settings", Icons.Filled.Settings),
}
