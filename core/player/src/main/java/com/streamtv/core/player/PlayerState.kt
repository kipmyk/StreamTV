package com.streamtv.core.player

import androidx.media3.common.PlaybackException

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLive: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val subtitle: String = "",
    val error: PlaybackException? = null
) {
    val progressPercentage: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
