package com.cheese.wallpaperscheduler.scheduler

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import com.cheese.wallpaperscheduler.data.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class ApplyResult {
    data object Success : ApplyResult()
    data class Failure(val reason: String) : ApplyResult()
}

/**
 * Applies a wallpaper image file to the system via WallpaperManager.
 *
 * Version handling: WallpaperManager.FLAG_SYSTEM / FLAG_LOCK (independent home vs. lock
 * screen wallpapers) exist since API 24, which is already below this app's minSdk 26, so no
 * SDK branching is needed for the flag API itself — the only version-sensitive bit is that
 * setting FLAG_LOCK on some OEM skins silently falls back to mirroring the home screen
 * wallpaper if the device doesn't support a distinct lock screen image. That's an OS/OEM
 * limitation we can't detect or work around; setBitmap() still reports success in that case.
 */
class WallpaperApplier(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)

    suspend fun apply(path: String, target: WallpaperTarget): ApplyResult = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) {
            return@withContext ApplyResult.Failure("Wallpaper file is missing")
        }

        val bitmap = try {
            decodeSampledBitmap(file)
        } catch (e: OutOfMemoryError) {
            Log.e("WallpaperApplier", "OOM decoding wallpaper", e)
            return@withContext ApplyResult.Failure("Image too large to load")
        } catch (e: Exception) {
            Log.e("WallpaperApplier", "Failed to decode wallpaper", e)
            return@withContext ApplyResult.Failure("Could not read image")
        }

        if (bitmap == null) {
            return@withContext ApplyResult.Failure("Could not decode image")
        }

        val flags = when (target) {
            WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
            WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
            WallpaperTarget.HOME_AND_LOCK -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        }

        try {
            wallpaperManager.setBitmap(bitmap, null, true, flags)
            ApplyResult.Success
        } catch (e: SecurityException) {
            Log.e("WallpaperApplier", "Not permitted to set wallpaper", e)
            ApplyResult.Failure("Not permitted to change the wallpaper")
        } catch (e: Exception) {
            Log.e("WallpaperApplier", "WallpaperManager rejected the wallpaper", e)
            ApplyResult.Failure("System rejected the wallpaper change")
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Decodes at a downsampled resolution sized to the device's screen rather than the
     * original (potentially huge) image, to avoid OOM on large photos.
     */
    private fun decodeSampledBitmap(file: File): Bitmap? {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val targetWidth = metrics.widthPixels
        val targetHeight = metrics.heightPixels

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

        var sampleSize = 1
        var halfWidth = boundsOptions.outWidth / 2
        var halfHeight = boundsOptions.outHeight / 2
        while (halfWidth / sampleSize >= targetWidth && halfHeight / sampleSize >= targetHeight) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
    }
}
