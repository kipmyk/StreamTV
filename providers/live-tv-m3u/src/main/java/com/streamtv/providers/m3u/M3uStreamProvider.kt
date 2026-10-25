package com.streamtv.providers.m3u

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult
import com.streamtv.core.network.NetworkClient
import com.streamtv.providers.api.MetadataProvider
import com.streamtv.providers.api.StreamProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class M3uStreamProvider(
    private val networkClient: NetworkClient = NetworkClient(),
    private val parser: M3uParser = M3uParser(),
    var currentSource: String? = null
) : StreamProvider, MetadataProvider {

    override val id: String = "provider_m3u"
    override val name: String = "Live TV (M3U Playlist)"
    override val supportedTypes: Set<ContentType> = setOf(ContentType.LIVE_TV, ContentType.SPORTS)

    private val mutex = Mutex()
    private val channelMap = mutableMapOf<String, M3uEntry>()
    private var globalEpgUrl: String? = null

    val isLoaded: Boolean
        get() = channelMap.isNotEmpty()

    val totalChannels: Int
        get() = channelMap.size

    suspend fun loadPlaylist(source: String): Result<Int> {
        val result = networkClient.get(source)
        return result.mapCatching { content ->
            if (content.isBlank()) {
                throw IllegalArgumentException("Playlist content is empty.")
            }
            val parsed = parser.parse(content)
            if (parsed.entries.isEmpty()) {
                throw IllegalArgumentException("No playable channels found in playlist.")
            }

            mutex.withLock {
                currentSource = source
                channelMap.clear()
                parsed.entries.forEach { entry ->
                    channelMap[entry.id] = entry
                }
                globalEpgUrl = parsed.globalEpgUrl
            }

            parsed.entries.size
        }
    }

    fun getGroups(): List<String> {
        return channelMap.values.mapNotNull { it.groupTitle }.distinct().sorted()
    }

    // StreamProvider Implementation
    override suspend fun canHandle(mediaItem: MediaItem): Boolean {
        return mediaItem.providerId == id || channelMap.containsKey(mediaItem.id) || mediaItem.directStreamUrl != null
    }

    override suspend fun resolveStream(
        mediaItem: MediaItem,
        season: Int?,
        episode: Int?
    ): StreamResult {
        val directUrl = mediaItem.directStreamUrl
        if (directUrl != null) {
            return StreamResult.Success(url = directUrl)
        }

        val entry = channelMap[mediaItem.id]
            ?: return StreamResult.Error("Channel '${mediaItem.title}' was not found in the loaded playlist.")

        if (entry.streamUrl.isBlank()) {
            return StreamResult.Error("Stream URL for channel '${entry.name}' is invalid or empty.")
        }

        return StreamResult.Success(
            url = entry.streamUrl,
            headers = entry.httpHeaders
        )
    }

    // MetadataProvider Implementation
    override suspend fun getTrending(type: ContentType): Result<List<MediaItem>> {
        return mutex.withLock {
            val items = channelMap.values.map { it.toMediaItem(id) }
            Result.success(items)
        }
    }

    override suspend fun getByCategory(category: String, type: ContentType): Result<List<MediaItem>> {
        return mutex.withLock {
            val items = channelMap.values
                .filter { it.groupTitle.equals(category, ignoreCase = true) }
                .map { it.toMediaItem(id) }
            Result.success(items)
        }
    }

    override suspend fun search(query: String, type: ContentType?): Result<List<MediaItem>> {
        return mutex.withLock {
            val items = channelMap.values
                .filter { it.name.contains(query, ignoreCase = true) || it.groupTitle?.contains(query, ignoreCase = true) == true }
                .map { it.toMediaItem(id) }
            Result.success(items)
        }
    }

    override suspend fun getDetails(mediaId: String, type: ContentType): Result<MediaItem> {
        val entry = channelMap[mediaId]
            ?: return Result.failure(NoSuchElementException("Channel with id $mediaId not found."))
        return Result.success(entry.toMediaItem(id))
    }
}

// Mapper extension
private fun M3uEntry.toMediaItem(providerId: String): MediaItem {
    return MediaItem(
        id = id,
        title = name,
        overview = "Live broadcast channel • Group: ${groupTitle ?: "General"}",
        posterUrl = tvgLogo,
        backdropUrl = tvgLogo,
        contentType = if (groupTitle?.contains("sport", ignoreCase = true) == true) ContentType.SPORTS else ContentType.LIVE_TV,
        directStreamUrl = streamUrl,
        providerId = providerId,
        category = groupTitle ?: "Live TV",
        groupTitle = groupTitle,
        tvgId = tvgId,
        tvgLogo = tvgLogo
    )
}
