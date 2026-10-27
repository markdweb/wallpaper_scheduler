package com.cheese.wallpaperscheduler.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheese.wallpaperscheduler.data.ScheduleSettings
import com.cheese.wallpaperscheduler.data.SettingsRepository
import com.cheese.wallpaperscheduler.data.ThemeMode
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import com.cheese.wallpaperscheduler.data.WallpaperImageStore
import com.cheese.wallpaperscheduler.data.WallpaperTarget
import com.cheese.wallpaperscheduler.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    val alarmScheduler: AlarmScheduler,
    private val imageStore: WallpaperImageStore,
) : ViewModel() {

    val settings: StateFlow<ScheduleSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setSchedulerEnabled(enabled: Boolean) = updateAndReschedule {
        repository.setSchedulerEnabled(enabled)
    }

    fun setWallpaperTarget(target: WallpaperTarget) = updateAndReschedule {
        repository.setWallpaperTarget(target)
    }

    fun setNotifyOnChange(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifyOnChange(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    /** Wipes settings and both stored wallpaper image files, then cancels any armed alarms. */
    fun resetSettings() {
        viewModelScope.launch {
            alarmScheduler.cancelAll()
            imageStore.remove(WallpaperSlot.LIGHT)
            imageStore.remove(WallpaperSlot.DARK)
            repository.resetAll()
        }
    }

    private fun updateAndReschedule(edit: suspend () -> Unit) {
        viewModelScope.launch {
            edit()
            val updated = repository.settingsFlow.first()
            alarmScheduler.rescheduleAll(updated)
        }
    }
}
