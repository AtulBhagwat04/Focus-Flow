package com.focusflow.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_channels")
data class StudyChannelEntity(
    @PrimaryKey val channelId: String,
    val channelTitle: String,
    val thumbnailUrl: String = "",
    val category: String = "General",
    val isCuratedDefault: Boolean = false,
    val isAllowlisted: Boolean = true,
)
