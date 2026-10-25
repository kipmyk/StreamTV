package com.streamtv.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface StreamResult {
    @Serializable
    data class Success(
        val url: String,
        val headers: Map<String, String> = emptyMap(),
        val subtitles: List<SubtitleTrack> = emptyList(),
        val drmScheme: String? = null
    ) : StreamResult

    @Serializable
    data class Error(
        val message: String,
        val throwableMessage: String? = null
    ) : StreamResult
}
