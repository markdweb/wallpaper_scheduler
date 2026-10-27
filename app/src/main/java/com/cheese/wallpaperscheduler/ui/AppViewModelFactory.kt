package com.cheese.wallpaperscheduler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cheese.wallpaperscheduler.WallpaperSchedulerApp
import com.cheese.wallpaperscheduler.ui.home.HomeViewModel
import com.cheese.wallpaperscheduler.ui.schedule.ScheduleViewModel
import com.cheese.wallpaperscheduler.ui.settings.SettingsViewModel
import com.cheese.wallpaperscheduler.ui.wallpapers.WallpapersViewModel

class AppViewModelFactory(private val app: WallpaperSchedulerApp) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm: ViewModel = when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(app.settingsRepository)

            modelClass.isAssignableFrom(WallpapersViewModel::class.java) ->
                WallpapersViewModel(app.settingsRepository, app.wallpaperImageStore, app.alarmScheduler, app)

            modelClass.isAssignableFrom(ScheduleViewModel::class.java) ->
                ScheduleViewModel(app.settingsRepository, app.alarmScheduler, app)

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(app.settingsRepository, app.alarmScheduler, app.wallpaperImageStore)

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return vm as T
    }
}
