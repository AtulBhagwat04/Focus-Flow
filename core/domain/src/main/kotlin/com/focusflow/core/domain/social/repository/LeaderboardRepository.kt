package com.focusflow.core.domain.social.repository

import com.focusflow.core.domain.social.model.LeaderboardEntry
import com.focusflow.core.domain.social.model.LeaderboardScope
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getWeeklyLeaderboard(scope: LeaderboardScope): Flow<List<LeaderboardEntry>>
    suspend fun refreshLeaderboard(scope: LeaderboardScope): Result<Unit>
}
