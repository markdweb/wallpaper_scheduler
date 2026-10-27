package com.cheese.wallpaperscheduler.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wallpaper_scheduler_settings")

/**
 * Single source of truth for everything the user configures. Everything is exposed through
 * one [settingsFlow] so ViewModels collect one StateFlow instead of juggling N individual keys.
 */
class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private object Keys {
        val SCHEDULER_ENABLED = booleanPreferencesKey("scheduler_enabled")
        val LIGHT_WALLPAPER_PATH = stringPreferencesKey("light_wallpaper_path")
        val DARK_WALLPAPER_PATH = stringPreferencesKey("dark_wallpaper_path")
        val LIGHT_HOUR = intPreferencesKey("light_hour")
        val LIGHT_MINUTE = intPreferencesKey("light_minute")
        val DARK_HOUR = intPreferencesKey("dark_hour")
        val DARK_MINUTE = intPreferencesKey("dark_minute")
        val WALLPAPER_TARGET = stringPreferencesKey("wallpaper_target")
        val NOTIFY_ON_CHANGE = booleanPreferencesKey("notify_on_change")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_APPLIED_SLOT = stringPreferencesKey("last_applied_slot")
    }

    val settingsFlow: Flow<ScheduleSettings> = dataStore.data.map { prefs ->
        ScheduleSettings(
            schedulerEnabled = prefs[Keys.SCHEDULER_ENABLED] ?: false,
            lightWallpaperPath = prefs[Keys.LIGHT_WALLPAPER_PATH],
            darkWallpaperPath = prefs[Keys.DARK_WALLPAPER_PATH],
            lightTime = TimeOfDay(
                prefs[Keys.LIGHT_HOUR] ?: TimeOfDay.DEFAULT_LIGHT.hour,
                prefs[Keys.LIGHT_MINUTE] ?: TimeOfDay.DEFAULT_LIGHT.minute,
            ),
            darkTime = TimeOfDay(
                prefs[Keys.DARK_HOUR] ?: TimeOfDay.DEFAULT_DARK.hour,
                prefs[Keys.DARK_MINUTE] ?: TimeOfDay.DEFAULT_DARK.minute,
            ),
            wallpaperTarget = WallpaperTarget.fromName(prefs[Keys.WALLPAPER_TARGET]),
            notifyOnChange = prefs[Keys.NOTIFY_ON_CHANGE] ?: false,
            themeMode = ThemeMode.fromName(prefs[Keys.THEME_MODE]),
            lastAppliedSlot = prefs[Keys.LAST_APPLIED_SLOT]?.let { WallpaperSlot.fromName(it) },
        )
    }

    suspend fun setSchedulerEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SCHEDULER_ENABLED] = enabled }
    }

    suspend fun setWallpaperPath(slot: WallpaperSlot, path: String?) {
        dataStore.edit { prefs ->
            val key = if (slot == WallpaperSlot.LIGHT) Keys.LIGHT_WALLPAPER_PATH else Keys.DARK_WALLPAPER_PATH
            if (path == null) prefs.remove(key) else prefs[key] = path
        }
    }

    suspend fun setTime(slot: WallpaperSlot, time: TimeOfDay) {
        dataStore.edit { prefs ->
            if (slot == WallpaperSlot.LIGHT) {
                prefs[Keys.LIGHT_HOUR] = time.hour
                prefs[Keys.LIGHT_MINUTE] = time.minute
            } else {
                prefs[Keys.DARK_HOUR] = time.hour
                prefs[Keys.DARK_MINUTE] = time.minute
            }
        }
    }

    suspend fun setWallpaperTarget(target: WallpaperTarget) {
        dataStore.edit { it[Keys.WALLPAPER_TARGET] = target.name }
    }

    suspend fun setNotifyOnChange(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFY_ON_CHANGE] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLastAppliedSlot(slot: WallpaperSlot) {
        dataStore.edit { it[Keys.LAST_APPLIED_SLOT] = slot.name }
    }

    /** Used by "Reset Settings" in the Settings screen. Wallpaper image files are cleared separately. */
    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}
