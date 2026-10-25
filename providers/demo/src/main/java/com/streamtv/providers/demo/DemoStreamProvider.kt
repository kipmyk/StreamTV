package com.streamtv.providers.demo

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult
import com.streamtv.providers.api.StreamProvider

class DemoStreamProvider : StreamProvider {
    override val id: String = "demo_stream"
    override val name: String = "Demo Stream Engine"
    override val supportedTypes: Set<ContentType> = ContentType.entries.toSet()

    // Public reliable known-good HLS test streams
    private val streamMap = mapOf(
        // Big Buck Bunny HLS
        "demo_movie_1" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        // Tears of Steel HLS
        "demo_movie_2" to "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        // Sintel / Series HLS
        "demo_series_1" to "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
        // Akamai Live HLS test
        "demo_live_1" to "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        // Sports Action HLS
        "demo_sports_1" to "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    )

    override suspend fun canHandle(mediaItem: MediaItem): Boolean {
        return streamMap.containsKey(mediaItem.id) || mediaItem.directStreamUrl != null
    }

    override suspend fun resolveStream(
        mediaItem: MediaItem,
        season: Int?,
        episode: Int?
    ): StreamResult {
        val url = mediaItem.directStreamUrl ?: streamMap[mediaItem.id]
        return if (url != null) {
            StreamResult.Success(url = url)
        } else {
            StreamResult.Error("No playable demo stream registered for ${mediaItem.title}")
        }
    }
}
