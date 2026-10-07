package com.focusflow.feature.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.social.model.FocusRoom
import com.focusflow.core.domain.social.model.LeaderboardScope
import com.focusflow.core.domain.social.model.PresenceState
import com.focusflow.core.domain.social.model.RoomParticipant
import com.focusflow.core.domain.social.model.UserPresence
import com.focusflow.core.domain.social.repository.FocusRoomRepository
import com.focusflow.core.domain.social.repository.FriendsRepository
import com.focusflow.core.domain.social.repository.LeaderboardRepository
import com.focusflow.core.domain.social.repository.PresenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val focusRoomRepository: FocusRoomRepository,
    private val presenceRepository: PresenceRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val friendsRepository: FriendsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(0)
    private val leaderboardScope = MutableStateFlow(LeaderboardScope.GLOBAL)
    private val isIncognito = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    // Dynamic participants observation for active room
    private val activeRoomParticipants: StateFlow<List<RoomParticipant>> =
        focusRoomRepository.getActiveRoom().flatMapLatest { activeRoom ->
            if (activeRoom != null) {
                presenceRepository.observeRoomPresence(activeRoom.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )

    private val leaderboardEntries = leaderboardScope.flatMapLatest { scope ->
        leaderboardRepository.getWeeklyLeaderboard(scope)
    }

    val uiState: StateFlow<RoomsUiState> = combine(
        selectedTab,
        focusRoomRepository.getPublicRooms(),
        focusRoomRepository.getActiveRoom(),
        activeRoomParticipants,
        leaderboardEntries,
        leaderboardScope,
        friendsRepository.getFriends(),
        isIncognito,
        message,
    ) { tab, rooms, activeRoom, participants, lbEntries, lbScope, friends, incognito, msg ->
        RoomsUiState(
            selectedTab = tab,
            publicRooms = rooms,
            activeRoom = activeRoom,
            activeRoomParticipants = participants,
            leaderboardEntries = lbEntries,
            leaderboardScope = lbScope,
            friends = friends,
            isIncognito = incognito,
            message = msg,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = RoomsUiState(isLoading = true),
    )

    fun onTabSelected(index: Int) {
        selectedTab.value = index
    }

    fun onLeaderboardScopeChanged(scope: LeaderboardScope) {
        leaderboardScope.value = scope
    }

    fun onCreateRoom(
        name: String,
        subjectTag: String,
        durationMinutes: Int,
        isPrivate: Boolean,
        accessCode: String?,
    ) {
        viewModelScope.launch {
            val user = authRepository.currentUser.value
            val room = FocusRoom(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Focus Room" },
                hostUserId = user?.id ?: "",
                hostDisplayName = user?.displayName ?: "Host",
                subjectTag = subjectTag.ifBlank { "General" },
                isPrivate = isPrivate,
                accessCode = if (isPrivate) accessCode else null,
                targetDurationMinutes = durationMinutes,
                participantCount = 1,
                createdAtTimestamp = System.currentTimeMillis(),
            )

            focusRoomRepository.createRoom(room).fold(
                onSuccess = { roomId ->
                    message.value = "Created and joined '${room.name}'."
                    // Also register room presence
                    user?.let { u ->
                        val presence = UserPresence(
                            userId = u.id,
                            displayName = u.displayName ?: "Host",
                            state = PresenceState.FOCUSING,
                            subjectTag = room.subjectTag,
                        )
                        presenceRepository.updatePresence(presence)
                    }
                },
                onFailure = { error ->
                    message.value = "Failed to create room: ${error.message}"
                },
            )
        }
    }

    fun onJoinRoom(roomId: String, accessCode: String? = null) {
        viewModelScope.launch {
            focusRoomRepository.joinRoom(roomId, accessCode).fold(
                onSuccess = {
                    message.value = "Joined room successfully."
                    val user = authRepository.currentUser.value
                    user?.let { u ->
                        presenceRepository.updatePresence(
                            UserPresence(
                                userId = u.id,
                                displayName = u.displayName ?: "Member",
                                state = PresenceState.FOCUSING,
                            )
                        )
                    }
                },
                onFailure = { error ->
                    message.value = "Could not join room: ${error.message}"
                },
            )
        }
    }

    fun onLeaveRoom(roomId: String) {
        viewModelScope.launch {
            focusRoomRepository.leaveRoom(roomId).fold(
                onSuccess = {
                    message.value = "Left room."
                    val user = authRepository.currentUser.value
                    user?.let { u ->
                        presenceRepository.updatePresence(
                            UserPresence(
                                userId = u.id,
                                displayName = u.displayName ?: "Member",
                                state = PresenceState.IDLE,
                            )
                        )
                    }
                },
                onFailure = { error ->
                    message.value = "Failed to leave: ${error.message}"
                },
            )
        }
    }

    fun onSendFriendRequest(inviteCode: String) {
        viewModelScope.launch {
            friendsRepository.sendFriendRequest(inviteCode).fold(
                onSuccess = {
                    message.value = "Friend added."
                },
                onFailure = { error ->
                    message.value = "Could not add friend: ${error.message}"
                },
            )
        }
    }

    fun onRemoveFriend(userId: String) {
        viewModelScope.launch {
            friendsRepository.removeFriend(userId).fold(
                onSuccess = {
                    message.value = "Friend removed."
                },
                onFailure = { error ->
                    message.value = "Failed to remove: ${error.message}"
                },
            )
        }
    }

    fun onToggleIncognito(enabled: Boolean) {
        viewModelScope.launch {
            isIncognito.value = enabled
            presenceRepository.setIncognito(enabled)
            message.value = if (enabled) "Incognito active: appearing offline." else "Live presence visible."
        }
    }

    fun onClearMessage() {
        message.value = null
    }
}
