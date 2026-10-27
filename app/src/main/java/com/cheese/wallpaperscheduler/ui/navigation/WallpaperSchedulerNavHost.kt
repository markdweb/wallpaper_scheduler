package com.cheese.wallpaperscheduler.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cheese.wallpaperscheduler.WallpaperSchedulerApp
import com.cheese.wallpaperscheduler.ui.AppViewModelFactory
import com.cheese.wallpaperscheduler.ui.home.HomeScreen
import com.cheese.wallpaperscheduler.ui.home.HomeViewModel
import com.cheese.wallpaperscheduler.ui.schedule.ScheduleScreen
import com.cheese.wallpaperscheduler.ui.schedule.ScheduleViewModel
import com.cheese.wallpaperscheduler.ui.settings.SettingsScreen
import com.cheese.wallpaperscheduler.ui.settings.SettingsViewModel
import com.cheese.wallpaperscheduler.ui.wallpapers.WallpapersScreen
import com.cheese.wallpaperscheduler.ui.wallpapers.WallpapersViewModel

@Composable
fun WallpaperSchedulerNavHost(app: WallpaperSchedulerApp) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(app)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar(
                // Edge-to-edge insets handled here rather than enableEdgeToEdge(), which is
                // known to cause bottom-nav tap/overlap issues on this stack.
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomNavItem.HOME.route) {
                val vm: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = vm,
                    alarmScheduler = app.alarmScheduler,
                    onEditSchedule = {
                        navController.navigate(BottomNavItem.SCHEDULE.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(BottomNavItem.WALLPAPERS.route) {
                val vm: WallpapersViewModel = viewModel(factory = factory)
                WallpapersScreen(viewModel = vm)
            }
            composable(BottomNavItem.SCHEDULE.route) {
                val vm: ScheduleViewModel = viewModel(factory = factory)
                ScheduleScreen(viewModel = vm)
            }
            composable(BottomNavItem.SETTINGS.route) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
