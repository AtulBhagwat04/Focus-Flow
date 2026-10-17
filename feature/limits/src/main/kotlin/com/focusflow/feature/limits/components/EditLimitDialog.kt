package com.focusflow.feature.limits.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusError
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.limits.AppLimitUiItem

private val DialogInputBg = Color(0xFFF9FBF9)

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
        shape = RoundedCornerShape(26.dp),
        containerColor = Color.White,
        title = {
            EditLimitDialogTitle(item = item)
        },
        text = {
            EditLimitDialogBody(
                minutesText = minutesText,
                onMinutesChange = { minutesText = it.filter { char -> char.isDigit() } },
                launchesText = launchesText,
                onLaunchesChange = { launchesText = it.filter { char -> char.isDigit() } },
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val minutes = minutesText.toIntOrNull()
                    val launches = launchesText.toIntOrNull()
                    onConfirm(minutes, launches)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusForestGreen,
                    contentColor = Color.White,
                ),
            ) {
                Text("Save Limit", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.dailyTimeLimitMinutes != null || item.dailyLaunchLimit != null) {
                    TextButton(
                        onClick = { onConfirm(null, null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = FocusError),
                    ) {
                        Text("Remove Limit")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = FocusMutedText),
                ) {
                    Text("Cancel")
                }
            }
        },
    )
}

@Composable
private fun EditLimitDialogTitle(item: AppLimitUiItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        InstalledAppIcon(
            packageName = item.packageName,
            appName = item.appName,
            size = 40.dp,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = item.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )
            Text(
                text = "Set Usage Limit",
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )
        }
    }
}

@Composable
private fun EditLimitDialogBody(
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    launchesText: String,
    onLaunchesChange: (String) -> Unit,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FocusForestGreen,
        focusedLabelColor = FocusForestGreen,
        cursorColor = FocusForestGreen,
        unfocusedBorderColor = FocusCardBorder,
        unfocusedContainerColor = DialogInputBg,
        focusedContainerColor = Color.White,
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Set a daily duration or launch count cap. Leave blank for no limit.",
            style = MaterialTheme.typography.bodySmall,
            color = FocusMutedText,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = minutesText,
            onValueChange = onMinutesChange,
            label = { Text("Daily Time Limit (minutes)") },
            placeholder = { Text("e.g. 45") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = launchesText,
            onValueChange = onLaunchesChange,
            label = { Text("Daily Launch Limit") },
            placeholder = { Text("e.g. 10") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
