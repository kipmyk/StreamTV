package com.streamtv.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SubtitleTrack(
    val language: String,
    val label: String,
    val url: String,
    val mimeType: String = "text/vtt"
)
