package com.cheese.wallpaperscheduler.data

/**
 * Where an applied wallpaper should show up. Maps directly onto WallpaperManager's
 * FLAG_SYSTEM / FLAG_LOCK flags in [com.cheese.wallpaperscheduler.scheduler.WallpaperApplier].
 */
enum class WallpaperTarget {
    HOME,
    LOCK,
    HOME_AND_LOCK;

    companion object {
        fun fromName(name: String?): WallpaperTarget =
            entries.firstOrNull { it.name == name } ?: HOME_AND_LOCK
    }
}

/** App-wide theme choice, independent of which wallpaper slot is currently active. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * Which of the two wallpaper slots a schedule entry / alarm refers to.
 * Modeled as its own type (rather than a raw boolean) so a third slot
 * (e.g. "afternoon") can be added later without an API break.
 */
enum class WallpaperSlot {
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): WallpaperSlot =
            entries.firstOrNull { it.name == name } ?: LIGHT
    }
}
