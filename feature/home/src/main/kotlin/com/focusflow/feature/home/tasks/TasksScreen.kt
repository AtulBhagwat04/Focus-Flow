package com.focusflow.feature.home.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.home.R

data class TaskItem(
    val id: String,
    val title: String,
    val durationMinutes: Int = 25,
    val isCompleted: Boolean = false,
)

/**
 * Tasks Screen matching the bottom navigation "Tasks" tab.
 * Gives the user a productive, clean checklist to plan and conquer daily deep-work items.
 */
@Composable
fun TasksScreen(
    onStartFocusOnTask: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val tasks = remember {
        mutableStateListOf(
            TaskItem("1", "Complete chapter 4 study session", 50, isCompleted = true),
            TaskItem("2", "Write project proposal draft", 25, isCompleted = false),
            TaskItem("3", "Review presentation slides", 25, isCompleted = false),
            TaskItem("4", "Organize study notes & references", 15, isCompleted = false),
        )
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Daily Tasks",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusDarkText,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = "${tasks.count { it.isCompleted }} of ${tasks.size} completed",
                        fontSize = 13.5.sp,
                        color = FocusMutedText,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FocusForestMintBg,
                    border = BorderStroke(1.dp, FocusCardBorder),
                ) {
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusForestGreenDeep,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Task List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = {
                            val index = tasks.indexOfFirst { it.id == task.id }
                            if (index != -1) {
                                tasks[index] = task.copy(isCompleted = !task.isCompleted)
                            }
                        },
                        onFocus = { onStartFocusOnTask(task.title, task.durationMinutes) },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Action Button to add tasks
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = FocusForestGreenDeep,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .shadow(6.dp, CircleShape),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add new task")
        }
    }

    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var durationMinutes by remember { mutableStateOf(25) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = {
                Text(text = "Add Focus Task", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            tasks.add(
                                TaskItem(
                                    id = System.currentTimeMillis().toString(),
                                    title = taskTitle.trim(),
                                    durationMinutes = durationMinutes,
                                )
                            )
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, color = FocusForestGreenDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0D000000),
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox Circle
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) FocusForestGreenDeep else Color(0xFFF1F5F2))
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onToggle,
                    )
                    .semantics {
                        role = Role.Checkbox
                        contentDescription = if (task.isCompleted) "Completed" else "Mark completed"
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Title & Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Color(0xFF94A3B8) else FocusDarkText,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${task.durationMinutes} min focus session",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                )
            }

            // Quick "Focus" button
            if (!task.isCompleted) {
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = onFocus,
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "Start focus session for ${task.title}"
                        },
                    shape = CircleShape,
                    color = FocusForestMintBg,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = FocusForestGreenDeep,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
