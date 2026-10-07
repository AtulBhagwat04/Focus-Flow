package com.focusflow.core.domain.model

/**
 * Ambient background audio tracks.
 *
 * Per PROJECT_BRIEF.md §3.2:
 * "Optional ambient sounds."
 */
enum class AmbientSound(
    val id: String,
    val displayName: String,
    val soundResourceName: String?,
) {
    NONE(
        id = "none",
        displayName = "Off",
        soundResourceName = null,
    ),
    WHITE_NOISE(
        id = "white_noise",
        displayName = "White Noise",
        soundResourceName = "ambient_white_noise",
    ),
    RAIN(
        id = "rain",
        displayName = "Gentle Rain",
        soundResourceName = "ambient_rain",
    ),
    FOREST(
        id = "forest",
        displayName = "Deep Forest",
        soundResourceName = "ambient_forest",
    ),
    STREAM(
        id = "stream",
        displayName = "Mountain Stream",
        soundResourceName = "ambient_stream",
    ),
    COFFEE_SHOP(
        id = "coffee_shop",
        displayName = "Coffee Shop",
        soundResourceName = "ambient_coffee_shop",
    );

    companion object {
        fun fromId(id: String?): AmbientSound =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}
