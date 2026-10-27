package com.cheese.wallpaperscheduler.ui.wallpapers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import com.cheese.wallpaperscheduler.ui.components.LocalWallpaperPreview

@Composable
fun WallpapersScreen(viewModel: WallpapersViewModel) {
    val settings by viewModel.settings.collectAsState()
    val current = settings ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Wallpapers", style = MaterialTheme.typography.headlineSmall)

        WallpaperSlotCard(
            title = "☀️ Light Wallpaper",
            slot = WallpaperSlot.LIGHT,
            path = current.lightWallpaperPath,
            onPicked = { uri -> viewModel.importWallpaper(uri, WallpaperSlot.LIGHT) },
            onRemove = { viewModel.removeWallpaper(WallpaperSlot.LIGHT) },
        )

        WallpaperSlotCard(
            title = "🌙 Dark Wallpaper",
            slot = WallpaperSlot.DARK,
            path = current.darkWallpaperPath,
            onPicked = { uri -> viewModel.importWallpaper(uri, WallpaperSlot.DARK) },
            onRemove = { viewModel.removeWallpaper(WallpaperSlot.DARK) },
        )
    }
}

@Composable
private fun WallpaperSlotCard(
    title: String,
    slot: WallpaperSlot,
    path: String?,
    onPicked: (Uri) -> Unit,
    onRemove: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            LocalWallpaperPreview(
                path = path,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(if (path == null) "Choose Wallpaper" else "Change")
                }
                if (path != null) {
                    OutlinedButton(onClick = onRemove) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}
