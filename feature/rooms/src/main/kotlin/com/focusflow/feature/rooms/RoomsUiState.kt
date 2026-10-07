package com.focusflow.feature.rooms

import com.focusflow.core.domain.social.model.FocusRoom
import com.focusflow.core.domain.social.model.Friend
import com.focusflow.core.domain.social.model.LeaderboardEntry
import com.focusflow.core.domain.social.model.LeaderboardScope
import com.focusflow.core.domain.social.model.RoomParticipant

data class RoomsUiState(
    val selectedTab: Int = 0,
    val publicRooms: List<FocusRoom> = emptyList(),
    val activeRoom: FocusRoom? = null,
    val activeRoomParticipants: List<RoomParticipant> = emptyList(),
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val leaderboardScope: LeaderboardScope = LeaderboardScope.GLOBAL,
    val friends: List<Friend> = emptyList(),
    val isIncognito: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
)
