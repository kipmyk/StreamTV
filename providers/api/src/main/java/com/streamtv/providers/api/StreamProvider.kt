package com.streamtv.providers.api

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult

/**
 * Interface responsible solely for resolving a playable media stream
 * (HLS m3u8, DASH mpd, or direct MP4/MKV) for a given MediaItem.
 */
interface StreamProvider {
    val id: String
    val name: String
    val supportedTypes: Set<ContentType>

    suspend fun canHandle(mediaItem: MediaItem): Boolean

    suspend fun resolveStream(
        mediaItem: MediaItem,
        season: Int? = null,
        episode: Int? = null
    ): StreamResult
}
