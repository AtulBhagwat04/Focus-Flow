package com.focusflow.feature.limits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusMutedText

@Composable
fun AddStudyChannelDialog(
    onDismiss: () -> Unit,
    onConfirm: (channelTitle: String) -> Unit,
) {
    var channelTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusForestMintBadge),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = FocusForestGreenDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Add Educational Channel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusDarkText,
                    )
                    Text(
                        text = "YouTube Study Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusMutedText,
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter the exact YouTube channel name you want to allow during study mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusMutedText,
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = channelTitle,
                    onValueChange = { channelTitle = it },
                    label = { Text("Channel Name") },
                    placeholder = { Text("e.g. 3Blue1Brown, Veritasium") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusForestGreen,
                        focusedLabelColor = FocusForestGreen,
                        cursorColor = FocusForestGreen,
                        unfocusedBorderColor = FocusCardBorder,
                        unfocusedContainerColor = Color(0xFFF9FBF9),
                        focusedContainerColor = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (channelTitle.isNotBlank()) {
                        onConfirm(channelTitle.trim())
                    }
                },
                enabled = channelTitle.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusForestGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2EBE5),
                    disabledContentColor = FocusMutedText,
                ),
            ) {
                Text("Add Channel", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = FocusMutedText),
            ) {
                Text("Cancel")
            }
        },
    )
}
