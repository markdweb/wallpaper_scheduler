package com.cheese.wallpaperscheduler.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cheese.wallpaperscheduler.data.SettingsRepository
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Runs when an AlarmManager alarm fires for a given slot. Uses goAsync() because the actual
 * work (read settings, decode a bitmap, call WallpaperManager) is asynchronous and must
 * finish before the receiver — and the temporary wake lock the system holds for it — is
 * allowed to end; without goAsync() the process could be frozen mid-operation.
 */
class WallpaperAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val slot = WallpaperSlot.fromName(intent.getStringExtra(AlarmScheduler.EXTRA_SLOT))
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                runApplyAndReschedule(context.applicationContext, slot)
            } catch (e: Exception) {
                Log.e("WallpaperAlarmReceiver", "Failed handling alarm for $slot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun runApplyAndReschedule(context: Context, slot: WallpaperSlot) {
        val repository = SettingsRepository(context)
        val settings = repository.settingsFlow.first()

        // The user may have disabled the scheduler or removed this wallpaper since the
        // alarm was armed; re-check before doing anything instead of trusting stale intent.
        if (!settings.schedulerEnabled) return

        val path = settings.pathFor(slot)
        if (path != null) {
            val result = WallpaperApplier(context).apply(path, settings.wallpaperTarget)
            repository.setLastAppliedSlot(slot)
            if (result is ApplyResult.Success && settings.notifyOnChange) {
                NotificationHelper(context).showWallpaperChangedNotification(slot)
            }
        }

        // Re-arm this same slot for 24 hours from now — this is what makes an "exact, once"
        // alarm behave like a daily repeating one without the inexactness of setRepeating().
        AlarmScheduler(context).schedule(slot, settings.timeFor(slot))
    }
}
