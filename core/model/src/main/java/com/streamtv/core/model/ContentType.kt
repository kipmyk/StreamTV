package com.streamtv.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ContentType {
    MOVIE,
    SERIES,
    LIVE_TV,
    SPORTS
}
