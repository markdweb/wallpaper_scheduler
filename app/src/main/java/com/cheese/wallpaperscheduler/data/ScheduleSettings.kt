package com.cheese.wallpaperscheduler.data

import java.time.LocalTime

/** Simple 24h hour/minute pair — avoids pulling LocalTime into DataStore's key types directly. */
data class TimeOfDay(val hour: Int, val minute: Int) {
    fun toLocalTime(): LocalTime = LocalTime.of(hour, minute)

    /** Minutes since midnight — handy for "which slot is active right now" comparisons. */
    fun toMinuteOfDay(): Int = hour * 60 + minute

    companion object {
        val DEFAULT_LIGHT = TimeOfDay(6, 0)
        val DEFAULT_DARK = TimeOfDay(17, 0)
    }
}

/**
 * Full, immutable snapshot of everything persisted in [com.cheese.wallpaperscheduler.data.SettingsRepository].
 * ViewModels collect this as a single StateFlow rather than subscribing to individual keys.
 */
data class ScheduleSettings(
    val schedulerEnabled: Boolean = false,
    val lightWallpaperPath: String? = null,
    val darkWallpaperPath: String? = null,
    val lightTime: TimeOfDay = TimeOfDay.DEFAULT_LIGHT,
    val darkTime: TimeOfDay = TimeOfDay.DEFAULT_DARK,
    val wallpaperTarget: WallpaperTarget = WallpaperTarget.HOME_AND_LOCK,
    val notifyOnChange: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastAppliedSlot: WallpaperSlot? = null,
) {
    /**
     * Given the current time, which slot (LIGHT/DARK) should be active. Handles the
     * overnight-wrap case where the dark window crosses midnight (e.g. 17:00 -> 06:00).
     */
    fun slotFor(nowMinuteOfDay: Int): WallpaperSlot {
        val light = lightTime.toMinuteOfDay()
        val dark = darkTime.toMinuteOfDay()
        return if (light <= dark) {
            // Normal case: light window is a contiguous span during the day.
            if (nowMinuteOfDay in light until dark) WallpaperSlot.LIGHT else WallpaperSlot.DARK
        } else {
            // Light window itself wraps past midnight (unusual custom schedule).
            if (nowMinuteOfDay in dark until light) WallpaperSlot.DARK else WallpaperSlot.LIGHT
        }
    }

    fun pathFor(slot: WallpaperSlot): String? = when (slot) {
        WallpaperSlot.LIGHT -> lightWallpaperPath
        WallpaperSlot.DARK -> darkWallpaperPath
    }

    fun timeFor(slot: WallpaperSlot): TimeOfDay = when (slot) {
        WallpaperSlot.LIGHT -> lightTime
        WallpaperSlot.DARK -> darkTime
    }
}
