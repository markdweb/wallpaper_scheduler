package com.cheese.wallpaperscheduler.ui.home

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheese.wallpaperscheduler.data.WallpaperSlot
import com.cheese.wallpaperscheduler.scheduler.AlarmScheduler
import com.cheese.wallpaperscheduler.ui.components.LocalWallpaperPreview
import com.cheese.wallpaperscheduler.util.formatted
import kotlinx.coroutines.delay
import java.time.LocalTime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    alarmScheduler: AlarmScheduler,
    onEditSchedule: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()

    // Ticks once a minute so "active until HH:MM" / which slot is showing stays correct
    // without requiring the user to leave and reopen the screen.
    var minuteTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            minuteTick++
        }
    }

    val current = settings ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Wallpaper Scheduler", style = MaterialTheme.typography.headlineSmall)

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (current.schedulerEnabled) "Scheduler ON" else "Scheduler Off",
                    style = MaterialTheme.typography.titleMedium,
                )
                Switch(
                    checked = current.schedulerEnabled,
                    onCheckedChange = { viewModel.setSchedulerEnabled(it, alarmScheduler) },
                )
            }
        }

        val nowMinuteOfDay = remember(minuteTick) { LocalTime.now().let { it.hour * 60 + it.minute } }
        val activeSlot = current.slotFor(nowMinuteOfDay)
        val activeUntil = current.timeFor(activeSlot.other())

        Text("Current Wallpaper", style = MaterialTheme.typography.titleMedium)

        LocalWallpaperPreview(
            path = current.pathFor(activeSlot),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f),
        )

        val (emoji, label) = if (activeSlot == WallpaperSlot.LIGHT) "☀️" to "Light Wallpaper" else "🌙" to "Dark Wallpaper"
        Text("$emoji $label", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Active until ${activeUntil.formatted()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text("Schedule", style = MaterialTheme.typography.titleMedium)

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScheduleRow("☀️", "Light Wallpaper", current.lightTime.formatted())
                ScheduleRow("🌙", "Dark Wallpaper", current.darkTime.formatted())
            }
        }

        Button(onClick = onEditSchedule, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Schedule")
        }
    }
}

@Composable
private fun ScheduleRow(emoji: String, label: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("$emoji $label")
        Text(time, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun WallpaperSlot.other(): WallpaperSlot =
    if (this == WallpaperSlot.LIGHT) WallpaperSlot.DARK else WallpaperSlot.LIGHT
