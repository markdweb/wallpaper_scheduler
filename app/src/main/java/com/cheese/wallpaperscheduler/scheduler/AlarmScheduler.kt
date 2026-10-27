package com.cheese.wallpaperscheduler.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.cheese.wallpaperscheduler.data.ScheduleSettings
import com.cheese.wallpaperscheduler.data.TimeOfDay
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Owns arming/disarming the two daily wallpaper-change alarms.
 *
 * Design choice: AlarmManager.setExactAndAllowWhileIdle(), NOT WorkManager and NOT
 * setRepeating(). Reasoning:
 *  - WorkManager's minimum periodic interval is 15 minutes and its timing is inexact by
 *    design (it batches to save battery), so a "change wallpaper at exactly 6:00 AM" request
 *    could slip by many minutes — unacceptable for a scheduler whose whole point is the time.
 *  - AlarmManager.setRepeating() is inexact on API 19+ for the same battery reasons.
 *  - setExactAndAllowWhileIdle() fires at the exact time (with a small OS-level delay budget)
 *    even in Doze, and each firing reschedules itself for +24h — this is Google's own
 *    recommended pattern for "exact, once-a-day" alarms.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_SLOT = "extra_slot"
        private const val REQUEST_CODE_LIGHT = 1001
        private const val REQUEST_CODE_DARK = 1002
    }

    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** Deep-links to the system screen where the user grants the exact-alarm permission (API 31+ only). */
    fun exactAlarmSettingsIntent(): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    /** Arms (or re-arms) both alarms from the current settings. No-op per slot if that wallpaper isn't set. */
    fun rescheduleAll(settings: ScheduleSettings) {
        if (!settings.schedulerEnabled) {
            cancelAll()
            return
        }
        if (settings.lightWallpaperPath != null) schedule(WallpaperSlot.LIGHT, settings.lightTime)
        if (settings.darkWallpaperPath != null) schedule(WallpaperSlot.DARK, settings.darkTime)
    }

    fun schedule(slot: WallpaperSlot, time: TimeOfDay) {
        val triggerAtMillis = nextTriggerMillis(time)
        val pendingIntent = pendingIntentFor(slot)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } catch (e: SecurityException) {
            // Exact-alarm permission was revoked after the app checked it (rare race on API 31-32
            // where the user can toggle it from system settings at any time). Fall back to inexact
            // so the schedule still eventually fires instead of silently doing nothing.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(slot: WallpaperSlot) {
        alarmManager.cancel(pendingIntentFor(slot))
    }

    fun cancelAll() {
        cancel(WallpaperSlot.LIGHT)
        cancel(WallpaperSlot.DARK)
    }

    /** Next occurrence of [time] from now — today if it hasn't passed yet, otherwise tomorrow. */
    private fun nextTriggerMillis(time: TimeOfDay): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var candidate = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.atZone(zone).toInstant().toEpochMilli()
    }

    private fun pendingIntentFor(slot: WallpaperSlot): PendingIntent {
        val requestCode = if (slot == WallpaperSlot.LIGHT) REQUEST_CODE_LIGHT else REQUEST_CODE_DARK
        val intent = Intent(context, WallpaperAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT, slot.name)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
