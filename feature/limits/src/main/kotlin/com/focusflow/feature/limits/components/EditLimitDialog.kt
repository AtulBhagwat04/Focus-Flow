package com.focusflow.feature.limits.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focusflow.feature.limits.AppLimitUiItem

@Composable
fun EditLimitDialog(
    item: AppLimitUiItem,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int?, launchLimit: Int?) -> Unit,
) {
    var minutesText by remember {
        mutableStateOf(item.dailyTimeLimitMinutes?.toString() ?: "")
    }
    var launchesText by remember {
        mutableStateOf(item.dailyLaunchLimit?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Limits: ${item.appName}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Time Limit (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 60 (leave blank for none)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = launchesText,
                    onValueChange = { launchesText = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Launch Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 15 (leave blank for none)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = minutesText.toIntOrNull()
                    val launches = launchesText.toIntOrNull()
                    onConfirm(minutes, launches)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
