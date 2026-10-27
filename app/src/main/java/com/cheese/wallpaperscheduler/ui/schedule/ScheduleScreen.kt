package com.cheese.wallpaperscheduler.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheese.wallpaperscheduler.data.TimeOfDay
import com.cheese.wallpaperscheduler.ui.components.AppTimePickerDialog
import com.cheese.wallpaperscheduler.util.formatted

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val settings by viewModel.settings.collectAsState()
    val current = settings ?: return

    var lightTime by remember(current.lightTime) { mutableStateOf(current.lightTime) }
    var darkTime by remember(current.darkTime) { mutableStateOf(current.darkTime) }
    var editingSlot by remember { mutableStateOf<EditingSlot?>(null) }
    var savedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Schedule", style = MaterialTheme.typography.headlineSmall)

        TimeRowCard(
            emoji = "☀️",
            label = "Light Wallpaper",
            time = lightTime,
            onClick = { editingSlot = EditingSlot.LIGHT },
        )

        TimeRowCard(
            emoji = "🌙",
            label = "Dark Wallpaper",
            time = darkTime,
            onClick = { editingSlot = EditingSlot.DARK },
        )

        Button(
            onClick = {
                viewModel.saveSchedule(lightTime, darkTime)
                savedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Schedule")
        }

        if (savedMessage) {
            Text(
                "Saved — schedule is active.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    editingSlot?.let { slot ->
        AppTimePickerDialog(
            title = if (slot == EditingSlot.LIGHT) "Light Wallpaper Start Time" else "Dark Wallpaper Start Time",
            initialTime = if (slot == EditingSlot.LIGHT) lightTime else darkTime,
            onDismiss = { editingSlot = null },
            onConfirm = { picked ->
                if (slot == EditingSlot.LIGHT) lightTime = picked else darkTime = picked
                editingSlot = null
                savedMessage = false
            },
        )
    }
}

private enum class EditingSlot { LIGHT, DARK }

@Composable
private fun TimeRowCard(emoji: String, label: String, time: TimeOfDay, onClick: () -> Unit) {
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
            Text("$emoji $label", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onClick) {
                Text(time.formatted())
            }
        }
    }
}
