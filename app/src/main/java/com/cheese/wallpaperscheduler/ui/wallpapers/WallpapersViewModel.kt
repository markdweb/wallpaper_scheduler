package com.cheese.wallpaperscheduler.ui.wallpapers

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheese.wallpaperscheduler.data.ScheduleSettings
import com.cheese.wallpaperscheduler.data.SettingsRepository
import com.cheese.wallpaperscheduler.data.WallpaperImageStore
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import com.cheese.wallpaperscheduler.scheduler.AlarmScheduler
import com.cheese.wallpaperscheduler.scheduler.WallpaperApplier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class WallpapersViewModel(
    private val repository: SettingsRepository,
    private val imageStore: WallpaperImageStore,
    private val alarmScheduler: AlarmScheduler,
    private val context: Context,
) : ViewModel() {

    val settings: StateFlow<ScheduleSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun importWallpaper(uri: Uri, slot: WallpaperSlot) {
        viewModelScope.launch {
            val path = imageStore.importFromUri(uri, slot)
            repository.setWallpaperPath(slot, path)
            applyIfDueRightNowAndRearm(slot)
        }
    }

    fun removeWallpaper(slot: WallpaperSlot) {
        viewModelScope.launch {
            imageStore.remove(slot)
            repository.setWallpaperPath(slot, null)
            val updated = repository.settingsFlow.first()
            if (updated.schedulerEnabled) {
                // Nothing to show for this slot anymore; cancel just that slot's alarm rather
                // than touching the other one.
                alarmScheduler.cancel(slot)
                if (updated.pathFor(slot.other()) != null) {
                    alarmScheduler.schedule(slot.other(), updated.timeFor(slot.other()))
                }
            }
        }
    }

    /** If the slot just changed is the one that should be showing right now, re-apply it immediately. */
    private suspend fun applyIfDueRightNowAndRearm(changedSlot: WallpaperSlot) {
        val updated = repository.settingsFlow.first()
        if (!updated.schedulerEnabled) return

        alarmScheduler.rescheduleAll(updated)

        val nowMinuteOfDay = LocalTime.now().let { it.hour * 60 + it.minute }
        val activeSlot = updated.slotFor(nowMinuteOfDay)
        if (activeSlot == changedSlot) {
            val path = updated.pathFor(changedSlot) ?: return
            WallpaperApplier(context).apply(path, updated.wallpaperTarget)
            repository.setLastAppliedSlot(changedSlot)
        }
    }

    private fun WallpaperSlot.other(): WallpaperSlot =
        if (this == WallpaperSlot.LIGHT) WallpaperSlot.DARK else WallpaperSlot.LIGHT
}
