package com.cheese.wallpaperscheduler

import android.app.Application
import com.cheese.wallpaperscheduler.data.SettingsRepository
import com.cheese.wallpaperscheduler.data.WallpaperImageStore
import com.cheese.wallpaperscheduler.scheduler.AlarmScheduler

/**
 * No DI framework — matches the rest of Cheese's Android projects. ViewModels reach these
 * through (application as WallpaperSchedulerApp).xxx.
 */
class WallpaperSchedulerApp : Application() {

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val wallpaperImageStore: WallpaperImageStore by lazy { WallpaperImageStore(this) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(this) }
}
