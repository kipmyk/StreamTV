package com.streamtv.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: String,
    val title: String,
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val contentType: ContentType,
    val releaseYear: String? = null,
    val rating: Double? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val directStreamUrl: String? = null,
    val providerId: String? = null,
    val category: String? = null,
    val genres: List<String> = emptyList(),
    val groupTitle: String? = null,
    val tvgId: String? = null,
    val tvgLogo: String? = null
)
