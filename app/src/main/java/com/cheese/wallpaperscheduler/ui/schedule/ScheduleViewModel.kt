package com.cheese.wallpaperscheduler.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheese.wallpaperscheduler.data.ScheduleSettings
import com.cheese.wallpaperscheduler.data.SettingsRepository
import com.cheese.wallpaperscheduler.data.TimeOfDay
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import com.cheese.wallpaperscheduler.scheduler.AlarmScheduler
import com.cheese.wallpaperscheduler.scheduler.WallpaperApplier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class ScheduleViewModel(
    private val repository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler,
    private val context: Context,
) : ViewModel() {

    val settings: StateFlow<ScheduleSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Persists both times, re-arms the alarms from the new times, and — per spec — applies
     * whichever wallpaper should be active under the *new* schedule right now, in case the
     * change just moved the boundary past or before the current moment.
     */
    fun saveSchedule(lightTime: TimeOfDay, darkTime: TimeOfDay) {
        viewModelScope.launch {
            repository.setTime(WallpaperSlot.LIGHT, lightTime)
            repository.setTime(WallpaperSlot.DARK, darkTime)

            val updated = repository.settingsFlow.first()
            if (!updated.schedulerEnabled) return@launch

            alarmScheduler.rescheduleAll(updated)

            val nowMinuteOfDay = LocalTime.now().let { it.hour * 60 + it.minute }
            val activeSlot = updated.slotFor(nowMinuteOfDay)
            val path = updated.pathFor(activeSlot) ?: return@launch
            WallpaperApplier(context).apply(path, updated.wallpaperTarget)
            repository.setLastAppliedSlot(activeSlot)
        }
    }
}
