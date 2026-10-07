package com.focusflow.feature.rooms

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.domain.social.model.FocusRoom
import com.focusflow.core.domain.social.model.LeaderboardScope
import com.focusflow.feature.rooms.components.ActiveRoomBanner
import com.focusflow.feature.rooms.components.CreateRoomDialog
import com.focusflow.feature.rooms.components.FriendsListTab
import com.focusflow.feature.rooms.components.LeaderboardItemCard
import com.focusflow.feature.rooms.components.NoChatNoticeCard
import com.focusflow.feature.rooms.components.RoomCard

@Composable
fun RoomsScreen(
    modifier: Modifier = Modifier,
    viewModel: RoomsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var joiningPrivateRoom by remember { mutableStateOf<FocusRoom?>(null) }
    var privatePasscodeInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onClearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Social & Focus Rooms",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Shared quiet presence & weekly standings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.onToggleIncognito(!uiState.isIncognito) },
                    ) {
                        Icon(
                            imageVector = if (uiState.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Incognito",
                            tint = if (uiState.isIncognito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                    }

                    Button(onClick = { showCreateRoomDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Host")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Room Banner if currently joined
            uiState.activeRoom?.let { room ->
                ActiveRoomBanner(
                    room = room,
                    participants = uiState.activeRoomParticipants,
                    onLeaveClicked = { viewModel.onLeaveRoom(room.id) },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            NoChatNoticeCard()

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            val tabs = listOf("Live Rooms", "Leaderboard", "Buddies")
            PrimaryTabRow(selectedTabIndex = uiState.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.selectedTab) {
                0 -> {
                    // Live Rooms Tab
                    if (uiState.publicRooms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No open study rooms right now",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showCreateRoomDialog = true }) {
                                    Text("Host the first room")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = uiState.publicRooms,
                                key = { it.id },
                            ) { room ->
                                RoomCard(
                                    room = room,
                                    onJoinClicked = {
                                        if (room.isPrivate) {
                                            joiningPrivateRoom = room
                                        } else {
                                            viewModel.onJoinRoom(room.id)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Leaderboard Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp),
                        ) {
                            FilterChip(
                                selected = uiState.leaderboardScope == LeaderboardScope.GLOBAL,
                                onClick = { viewModel.onLeaderboardScopeChanged(LeaderboardScope.GLOBAL) },
                                label = { Text("Global") },
                            )
                            FilterChip(
                                selected = uiState.leaderboardScope == LeaderboardScope.FRIENDS,
                                onClick = { viewModel.onLeaderboardScopeChanged(LeaderboardScope.FRIENDS) },
                                label = { Text("Buddies") },
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = uiState.leaderboardEntries,
                                key = { it.userId },
                            ) { entry ->
                                LeaderboardItemCard(entry = entry)
                            }
                        }
                    }
                }

                2 -> {
                    // Friends Tab
                    FriendsListTab(
                        friends = uiState.friends,
                        onAddFriendClicked = viewModel::onSendFriendRequest,
                        onRemoveFriendClicked = viewModel::onRemoveFriend,
                    )
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onConfirm = { name, tag, duration, isPrivate, code ->
                showCreateRoomDialog = false
                viewModel.onCreateRoom(name, tag, duration, isPrivate, code)
            },
        )
    }

    joiningPrivateRoom?.let { room ->
        AlertDialog(
            onDismissRequest = {
                joiningPrivateRoom = null
                privatePasscodeInput = ""
            },
            title = { Text("Enter Room Passcode") },
            text = {
                OutlinedTextField(
                    value = privatePasscodeInput,
                    onValueChange = { privatePasscodeInput = it },
                    label = { Text("Passcode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val code = privatePasscodeInput
                        val targetRoomId = room.id
                        joiningPrivateRoom = null
                        privatePasscodeInput = ""
                        viewModel.onJoinRoom(targetRoomId, code)
                    },
                    enabled = privatePasscodeInput.isNotBlank(),
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    joiningPrivateRoom = null
                    privatePasscodeInput = ""
                }) {
                    Text("Cancel")
                }
            },
        )
    }
}
