package com.cheese.wallpaperscheduler.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.Manifest
import android.os.Build
import com.cheese.wallpaperscheduler.data.ThemeMode
import com.cheese.wallpaperscheduler.data.WallpaperTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val current = settings ?: return
    var showResetConfirm by remember { mutableStateOf(false) }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* user returns from system settings; next toggle re-checks */ }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* NotificationHelper re-checks the grant before posting */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        SettingsSection(title = "Scheduler") {
            SettingsRow(label = "Enable Scheduler") {
                Switch(
                    checked = current.schedulerEnabled,
                    onCheckedChange = { viewModel.setSchedulerEnabled(it) },
                )
            }
            Text(
                if (current.schedulerEnabled) "Status: active" else "Status: off",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!viewModel.alarmScheduler.canScheduleExactAlarms()) {
                OutlinedButton(onClick = {
                    exactAlarmSettingsLauncher.launch(viewModel.alarmScheduler.exactAlarmSettingsIntent())
                }) {
                    Text("Grant exact-alarm permission")
                }
            }
        }

        SettingsSection(title = "Wallpaper Target") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    WallpaperTarget.HOME to "Home",
                    WallpaperTarget.LOCK to "Lock",
                    WallpaperTarget.HOME_AND_LOCK to "Both",
                )
                options.forEachIndexed { index, (target, label) ->
                    SegmentedButton(
                        selected = current.wallpaperTarget == target,
                        onClick = { viewModel.setWallpaperTarget(target) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    ) {
                        Text(label)
                    }
                }
            }
        }

        SettingsSection(title = "Notifications") {
            SettingsRow(label = "Notify when wallpaper changes") {
                Switch(
                    checked = current.notifyOnChange,
                    onCheckedChange = { enabled ->
                        viewModel.setNotifyOnChange(enabled)
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
            }
        }

        SettingsSection(title = "App Theme") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    ThemeMode.SYSTEM to "System",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.DARK to "Dark",
                )
                options.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = current.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    ) {
                        Text(label)
                    }
                }
            }
        }

        SettingsSection(title = "Other") {
            OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset Settings")
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset all settings?") },
            text = { Text("This clears your schedule, wallpapers, and preferences. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetSettings()
                    showResetConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        control()
    }
}
