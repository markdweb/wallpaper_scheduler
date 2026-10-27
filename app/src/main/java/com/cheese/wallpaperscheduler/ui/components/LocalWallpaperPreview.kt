package com.cheese.wallpaperscheduler.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shows the image at [path] scaled to fill, or a placeholder message when [path] is null
 * or the file no longer exists on disk (e.g. the user cleared app storage manually).
 */
@Composable
fun LocalWallpaperPreview(path: String?, modifier: Modifier = Modifier, placeholder: String = "No wallpaper set") {
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = path?.let { decodeOffMain(it) }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private const val PREVIEW_MAX_DIMENSION = 900

/** Downsampled decode — previews don't need full wallpaper resolution, and this avoids OOM on large photos. */
private suspend fun decodeOffMain(path: String): ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > PREVIEW_MAX_DIMENSION || bounds.outHeight / sampleSize > PREVIEW_MAX_DIMENSION) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeFile(path, options)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
