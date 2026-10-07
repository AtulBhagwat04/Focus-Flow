package com.focusflow.core.domain.blocking.model

data class RemoteDetectionSelectors(
    val youtubeShortsViewIds: List<String> = listOf("shorts_container", "reel_recycler_view"),
    val instagramReelsViewIds: List<String> = listOf("clips_video_container", "reel_viewer_root"),
    val killSwitchEnabled: Boolean = false,
)
