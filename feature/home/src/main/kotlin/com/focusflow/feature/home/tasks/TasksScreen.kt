package com.focusflow.feature.home.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusCircleRadioBorder
import com.focusflow.core.designsystem.theme.FocusCompletedCardBg
import com.focusflow.core.designsystem.theme.FocusCompletedCardBorder
import com.focusflow.core.designsystem.theme.FocusCompletedMutedText
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusGaugeArc
import com.focusflow.core.designsystem.theme.FocusGaugeTrack
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusScreenBackground
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.feature.home.R

/**
 * Daily Tasks screen matching the editorial, serene design reference:
 * - Backed by [TasksViewModel] and [TaskRepository] single source of truth.
 * - When user clicks Play on a task, auto-manages the focus session duration on Focus Screen.
 * - Displays live progress (completed focus time vs target) on each task card.
 */
@Composable
fun TasksScreen(
    onStartFocusOnTask: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activeTask by viewModel.activeTask.collectAsStateWithLifecycle()

    TasksScreenContent(
        tasks = tasks,
        activeTask = activeTask,
        onToggleTask = viewModel::onToggleTask,
        onStartFocus = { task ->
            viewModel.onStartFocus(task.id, onStartFocusOnTask)
        },
        onAddTask = viewModel::onAddTask,
        modifier = modifier,
    )
}

@Composable
fun TasksScreenContent(
    tasks: List<FocusTask>,
    activeTask: FocusTask?,
    onToggleTask: (String) -> Unit,
    onStartFocus: (FocusTask) -> Unit,
    onAddTask: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val completedTasks = tasks.filter { it.isCompleted }
    val upNextTasks = tasks.filter { !it.isCompleted }

    val totalCount = tasks.size
    val completedCount = completedTasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val percentage = (progressFraction * 100).toInt()

    val totalFocusMinutes = tasks.sumOf { it.durationMinutes }
    val plannedHours = totalFocusMinutes / 60
    val plannedMinutes = totalFocusMinutes % 60
    val plannedFocusText = when {
        totalFocusMinutes == 0 -> stringResource(R.string.tasks_summary_planned_empty)
        plannedHours > 0 -> stringResource(R.string.tasks_summary_planned_today_hours, plannedHours, plannedMinutes)
        else -> stringResource(R.string.tasks_summary_planned_today_mins, plannedMinutes)
    }

    CompositionLocalProvider(LocalContentColor provides FocusDarkText) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(FocusScreenBackground),
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                TasksHeaderRow(
                    onTodayClick = { /* Can show date picker or history */ },
                )
            }

            if (tasks.isNotEmpty()) {
                item {
                    TasksProgressBanner(
                        completedCount = completedCount,
                        totalCount = totalCount,
                        percentage = percentage,
                        progressFraction = progressFraction,
                        plannedFocusText = plannedFocusText,
                    )
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0x181B4E36),
                                spotColor = Color(0x221B4E36),
                            ),
                        shape = RoundedCornerShape(22.dp),
                        color = FocusForestGreenDeep,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2B5643),
                                modifier = Modifier.size(48.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = FocusGaugeArc,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to focus?",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Add tasks to plan your focus time today.",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFD6EDE2),
                                )
                            }
                        }
                    }
                }
            }

            // "Up next" Section
            if (upNextTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    TasksSectionHeader(
                        title = stringResource(R.string.tasks_section_up_next),
                        countLabel = if (upNextTasks.size == 1) {
                            stringResource(R.string.tasks_count_left_singular)
                        } else {
                            stringResource(R.string.tasks_count_left_format, upNextTasks.size)
                        },
                    )
                }

                items(upNextTasks, key = { it.id }) { task ->
                    UpNextTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task.id) },
                        onFocus = { onStartFocus(task) },
                    )
                }
            }

            // "Completed" Section
            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    TasksSectionHeader(
                        title = stringResource(R.string.tasks_section_completed),
                        countLabel = stringResource(R.string.tasks_count_done_format, completedTasks.size),
                    )
                }

                items(completedTasks, key = { it.id }) { task ->
                    CompletedTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task.id) },
                    )
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, FocusCardBorder),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FocusForestMintBg,
                                modifier = Modifier.size(56.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = FocusForestGreenDeep,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No tasks planned yet",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = FocusDarkText,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add what you want to work on today. When you start, your focus timer will calibrate automatically.",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.5.sp,
                                color = FocusMutedText,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { showAddTaskDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FocusForestGreenDeep,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add First Task",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Bottom spacing for comfortable scroll above floating nav bar and FAB
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Circular Floating Action Button (+)
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = FocusForestGreenDeep,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 14.dp)
                .size(52.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x301B4E36),
                    spotColor = Color(0x401B4E36),
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.tasks_cd_add_task),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, duration ->
                onAddTask(title, duration)
                showAddTaskDialog = false
            },
        )
    }
}
}

/**
 * Top row featuring the serif title "Daily Tasks", subtle tagline, and pill "Today" chip.
 */
@Composable
private fun TasksHeaderRow(
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.tasks_title),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = FocusDarkText,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.tasks_subtitle),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // "Today" Pill Chip
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFE2EFE7),
            border = BorderStroke(1.dp, Color(0xFFD0E4D8)),
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    indication = ripple(bounded = true),
                    interactionSource = null,
                    onClick = onTodayClick,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = "Filter by date: Today"
                },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = FocusForestGreenDeep,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.tasks_chip_today),
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusForestGreenDeep,
                )
            }
        }
    }
}

/**
 * Forest green banner card summarizing completed vs total tasks with an animated circular gauge.
 */
@Composable
private fun TasksProgressBanner(
    completedCount: Int,
    totalCount: Int,
    percentage: Int,
    progressFraction: Float,
    plannedFocusText: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "progress_banner_gauge",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x201B4E36),
                spotColor = Color(0x2C1B4E36),
            ),
        shape = RoundedCornerShape(22.dp),
        color = FocusForestGreenDeep,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Circular progress gauge
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(52.dp)) {
                    val strokeWidth = 5.dp.toPx()
                    // Background track ring
                    drawCircle(
                        color = FocusGaugeTrack,
                        style = Stroke(width = strokeWidth),
                    )
                    // Animated arc
                    drawArc(
                        color = FocusGaugeArc,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Text(
                    text = "$percentage%",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Completion headline and planned duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tasks_summary_completed_format, completedCount, totalCount),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plannedFocusText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFFB4D3C5),
                )
            }
        }
    }
}

/**
 * Section title with count label (e.g. "Up next" / "3 tasks left").
 */
@Composable
private fun TasksSectionHeader(
    title: String,
    countLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = FocusDarkText,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = countLabel,
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            color = FocusMutedText,
        )
    }
}

/**
 * Card for an uncompleted task: clean white pill, circular radio button, serif title,
 * session time indicator, live progress bar, and soft mint play button.
 */
@Composable
private fun UpNextTaskCard(
    task: FocusTask,
    onToggle: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (task.isCurrentFocus) FocusForestGreen else FocusCardBorder
    val borderWidth = if (task.isCurrentFocus) 1.2.dp else 1.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (task.isCurrentFocus) 3.dp else 1.5.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000),
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Radio circle to mark completed
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onToggle,
                    )
                    .semantics {
                        role = Role.Checkbox
                        contentDescription = "Mark ${task.title} as completed"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, FocusCircleRadioBorder),
                    modifier = Modifier.size(19.dp),
                ) {}
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & duration / progress
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FocusDarkText,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (task.isCurrentFocus) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FocusForestMintBg,
                        ) {
                            Text(
                                text = "Focused now",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusForestGreenDeep,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = FocusMutedText,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.tasks_session_duration_format, task.durationMinutes),
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.5.sp,
                        color = FocusMutedText,
                    )
                    if (task.completedSeconds > 0) {
                        Text(
                            text = " • ${task.formattedCompletedTime} done",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusForestGreen,
                        )
                    }
                }

                // Show mini progress bar if task has progress
                if (task.completedSeconds > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { task.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(CircleShape),
                        color = FocusForestGreenDeep,
                        trackColor = Color(0xFFE2EBE5),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Circular Play Button to start auto-managed focus session
            Surface(
                modifier = Modifier
                    .size(32.dp)
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
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

/**
 * Card for a completed task: soft sage background, dark forest checkmark, strikethrough title.
 */
@Composable
private fun CompletedTaskCard(
    task: FocusTask,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FocusCompletedCardBg,
        border = BorderStroke(1.dp, FocusCompletedCardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checked Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(FocusForestGreenDeep)
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onToggle,
                    )
                    .semantics {
                        role = Role.Checkbox
                        contentDescription = "Mark ${task.title} as incomplete"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Strikethrough Title & duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FocusCompletedMutedText,
                    textDecoration = TextDecoration.LineThrough,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.tasks_session_duration_format, task.durationMinutes),
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.5.sp,
                    color = FocusCompletedMutedText,
                )
            }
        }
    }
}

/**
 * Dialog to add a new focus task and configure its timer:
 * - 3 predefined duration options: 25 min, 50 min, 100 min.
 * - Custom timer option allowing direct numeric entry and stepper controls.
 * - Fully accessible with guaranteed high-contrast font colors in light & dark themes.
 */
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, Int) -> Unit,
) {
    var taskTitle by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableIntStateOf(25) }
    var isCustomSelected by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("30") }
    val customFocusRequester = remember { FocusRequester() }

    val effectiveDuration = if (isCustomSelected) {
        val parsed = customMinutesText.toIntOrNull()
        if (parsed != null && parsed > 0) parsed.coerceAtMost(360) else 0
    } else {
        selectedPreset
    }

    LaunchedEffect(isCustomSelected) {
        if (isCustomSelected) {
            customFocusRequester.requestFocus()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CompositionLocalProvider(LocalContentColor provides FocusDarkText) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0x18000000),
                        spotColor = Color(0x28000000),
                    ),
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 22.dp),
                ) {
                    // Header row: Icon badge & strong effective title (subtitle removed as requested)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FocusForestMintBg,
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = FocusForestGreenDeep,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = stringResource(R.string.tasks_dialog_title),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = FocusDarkText,
                            letterSpacing = (-0.4).sp,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Task Title Input (auto-adjusts according to content up to 3 lines, max 80 chars)
                    Text(
                        text = stringResource(R.string.tasks_dialog_input_label).uppercase(),
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = FocusMutedText,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { input ->
                            if (input.length <= 80) {
                                taskTitle = input
                            }
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.tasks_dialog_input_placeholder),
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF7E9489),
                            )
                        },
                        minLines = 1,
                        maxLines = 3,
                        textStyle = TextStyle(
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusDarkText,
                        ),
                        trailingIcon = {
                            if (taskTitle.isNotEmpty()) {
                                IconButton(onClick = { taskTitle = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.tasks_sheet_clear_input),
                                        tint = FocusMutedText,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FocusDarkText,
                            unfocusedTextColor = FocusDarkText,
                            focusedContainerColor = Color(0xFFFAFCFA),
                            unfocusedContainerColor = Color(0xFFF5FAF7),
                            focusedBorderColor = FocusForestGreenDeep,
                            unfocusedBorderColor = Color(0xFFD6E4DB),
                            cursorColor = FocusForestGreenDeep,
                            focusedPlaceholderColor = Color(0xFF7E9489),
                            unfocusedPlaceholderColor = Color(0xFF7E9489),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Focus Timer Duration Section Header (duration badge removed as requested)
                    Text(
                        text = stringResource(R.string.tasks_dialog_duration_label).uppercase(),
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = FocusMutedText,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Duration Options: 3 Presets (25, 50, 100 min) + In-Place Custom Input
                    val presets = listOf(25, 50, 100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 25 min, 50 min, 100 min presets
                        presets.forEach { preset ->
                            val isSelected = !isCustomSelected && selectedPreset == preset
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FocusForestGreenDeep else Color(0xFFF1F6F3),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FocusForestGreenDeep else Color(0xFFDCE7E1),
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isCustomSelected = false
                                        selectedPreset = preset
                                    },
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${preset}m",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else FocusDarkText,
                                    )
                                }
                            }
                        }

                        // 4th Slot: In-place Custom Option / Input
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCustomSelected) Color.White else Color(0xFFF1F6F3),
                            border = BorderStroke(
                                if (isCustomSelected) 1.5.dp else 1.dp,
                                if (isCustomSelected) FocusForestGreenDeep else Color(0xFFDCE7E1),
                            ),
                            modifier = Modifier
                                .weight(1.25f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isCustomSelected) {
                                        isCustomSelected = true
                                    }
                                },
                        ) {
                            if (!isCustomSelected) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.tasks_dialog_custom),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FocusDarkText,
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BasicTextField(
                                        value = customMinutesText,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }
                                            if (digits.length <= 3) {
                                                customMinutesText = digits
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        textStyle = TextStyle(
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = FocusForestGreenDeep,
                                        ),
                                        cursorBrush = SolidColor(FocusForestGreenDeep),
                                        modifier = Modifier
                                            .focusRequester(customFocusRequester)
                                            .widthIn(min = 18.dp, max = 34.dp),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.Center) {
                                                if (customMinutesText.isEmpty()) {
                                                    Text(
                                                        text = "0",
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFA5B8AD),
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        },
                                    )
                                    Text(
                                        text = "m",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FocusForestGreenDeep,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. Action Buttons (Cancel + Create Task - equal 1f size, duration/arrow removed)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Cancel Button (weight 1f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = onDismiss,
                                ),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_dialog_btn_cancel),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B),
                                )
                            }
                        }

                        // Create Task Button (weight 1f, centered label)
                        val isEnabled = taskTitle.isNotBlank() && effectiveDuration > 0
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .shadow(
                                    elevation = if (isEnabled) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = Color(0x201B4E36),
                                    spotColor = Color(0x351B4E36),
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    enabled = isEnabled,
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = {
                                        if (isEnabled) {
                                            onAddTask(taskTitle.trim(), effectiveDuration)
                                        }
                                    },
                                ),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isEnabled) FocusForestGreenDeep else Color(0xFFDCE6DF),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_dialog_btn_add),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isEnabled) Color.White else Color(0xFF8AA094),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7FAF8)
@Composable
private fun TasksScreenPreview() {
    TasksScreenContent(
        tasks = listOf(
            FocusTask("1", "Write project proposal draft", 25, completedSeconds = 300, isCompleted = false),
            FocusTask("2", "Review presentation slides", 25, completedSeconds = 0, isCompleted = false),
            FocusTask("3", "Organize study notes & references", 15, completedSeconds = 0, isCompleted = false),
            FocusTask("4", "Complete chapter 4 study session", 50, completedSeconds = 50 * 60, isCompleted = true),
        ),
        activeTask = null,
        onToggleTask = {},
        onStartFocus = {},
        onAddTask = { _, _ -> },
    )
}
