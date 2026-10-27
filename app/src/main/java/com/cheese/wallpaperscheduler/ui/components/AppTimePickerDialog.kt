@file:OptIn(ExperimentalMaterial3Api::class)
package com.cheese.wallpaperscheduler.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cheese.wallpaperscheduler.data.TimeOfDay

/**
 * M3 ships the TimePicker widget itself but not a ready-made dialog around it (unlike
 * DatePickerDialog) as of this Compose BOM — so this wraps it in a plain Dialog + Surface.
 */
@Composable
fun AppTimePickerDialog(
    title: String,
    initialTime: TimeOfDay,
    onDismiss: () -> Unit,
    onConfirm: (TimeOfDay) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(TimeOfDay(state.hour, state.minute)) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
