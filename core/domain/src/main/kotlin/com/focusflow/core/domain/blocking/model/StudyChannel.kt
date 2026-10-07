package com.focusflow.core.domain.blocking.model

data class StudyChannel(
    val channelId: String,
    val channelTitle: String,
    val thumbnailUrl: String = "",
    val category: String = "General",
    val isCuratedDefault: Boolean = false,
    val isAllowlisted: Boolean = true,
)
