package com.cheese.wallpaperscheduler.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * The Android Photo Picker only grants read access to the URIs it returns until the next
 * device reboot — it's not a persistable grant, so calling takePersistableUriPermission()
 * on it throws. Since this app needs to read the wallpaper file every day, indefinitely,
 * we copy the picked image into app-private internal storage immediately after selection
 * and keep only that local file path in DataStore. No storage permission needed either way.
 */
class WallpaperImageStore(private val context: Context) {

    private val wallpaperDir: File
        get() = File(context.filesDir, "wallpapers").apply { mkdirs() }

    /** Copies [uri] into internal storage as this slot's wallpaper, replacing any previous one. */
    suspend fun importFromUri(uri: Uri, slot: WallpaperSlot): String = withContext(Dispatchers.IO) {
        val destination = File(wallpaperDir, fileNameFor(slot))
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open the selected image")
        destination.absolutePath
    }

    suspend fun remove(slot: WallpaperSlot) = withContext(Dispatchers.IO) {
        File(wallpaperDir, fileNameFor(slot)).delete()
        Unit
    }

    /** True if the file backing [path] is still present on disk (guards against manual deletion, etc). */
    fun exists(path: String?): Boolean = path != null && File(path).exists()

    private fun fileNameFor(slot: WallpaperSlot) = when (slot) {
        WallpaperSlot.LIGHT -> "light_wallpaper.jpg"
        WallpaperSlot.DARK -> "dark_wallpaper.jpg"
    }
}
