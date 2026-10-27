package com.cheese.wallpaperscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cheese.wallpaperscheduler.data.ThemeMode
import com.cheese.wallpaperscheduler.ui.navigation.WallpaperSchedulerNavHost
import com.cheese.wallpaperscheduler.ui.theme.WallpaperSchedulerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as WallpaperSchedulerApp

        setContent {
            val settings by app.settingsRepository.settingsFlow.collectAsState(initial = null)
            val darkOverride = when (settings?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> null // SYSTEM or not loaded yet
            }

            WallpaperSchedulerTheme(darkTheme = darkOverride) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WallpaperSchedulerNavHost(app = app)
                }
            }
        }
    }
}
