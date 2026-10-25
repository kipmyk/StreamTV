package com.streamtv.app.provider

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult
import com.streamtv.providers.api.MetadataProvider
import com.streamtv.providers.api.StreamProvider
import com.streamtv.providers.demo.DemoMetadataProvider
import com.streamtv.providers.demo.DemoStreamProvider
import com.streamtv.providers.m3u.M3uStreamProvider
import com.streamtv.providers.tmdb.TmdbConfig
import com.streamtv.providers.tmdb.TmdbMetadataProvider

class CompositeMetadataProvider(
    private val tmdbProvider: TmdbMetadataProvider,
    private val m3uProvider: M3uStreamProvider,
    private val demoProvider: DemoMetadataProvider = DemoMetadataProvider(),
    private val tmdbConfig: TmdbConfig
) : MetadataProvider {

    override val id: String = "composite_metadata"
    override val name: String = "Composite Metadata Engine"
    override val supportedTypes: Set<ContentType> = ContentType.entries.toSet()

    override suspend fun getTrending(type: ContentType): Result<List<MediaItem>> {
        return when (type) {
            ContentType.MOVIE, ContentType.SERIES -> {
                if (tmdbConfig.isConfigured) {
                    val tmdbResult = tmdbProvider.getTrending(type)
                    if (tmdbResult.isSuccess && tmdbResult.getOrNull()?.isNotEmpty() == true) {
                        return tmdbResult
                    }
                }
                demoProvider.getTrending(type)
            }
            ContentType.LIVE_TV, ContentType.SPORTS -> {
                if (m3uProvider.isLoaded) {
                    val m3uResult = m3uProvider.getTrending(type)
                    if (m3uResult.isSuccess && m3uResult.getOrNull()?.isNotEmpty() == true) {
                        return m3uResult
                    }
                }
                demoProvider.getTrending(type)
            }
        }
    }

    override suspend fun getByCategory(category: String, type: ContentType): Result<List<MediaItem>> {
        if (type == ContentType.MOVIE || type == ContentType.SERIES) {
            if (tmdbConfig.isConfigured) {
                val tmdbResult = tmdbProvider.getByCategory(category, type)
                if (tmdbResult.isSuccess && tmdbResult.getOrNull()?.isNotEmpty() == true) {
                    return tmdbResult
                }
            }
        } else if (type == ContentType.LIVE_TV && m3uProvider.isLoaded) {
            val m3uResult = m3uProvider.getByCategory(category, type)
            if (m3uResult.isSuccess && m3uResult.getOrNull()?.isNotEmpty() == true) {
                return m3uResult
            }
        }
        return demoProvider.getByCategory(category, type)
    }

    override suspend fun search(query: String, type: ContentType?): Result<List<MediaItem>> {
        val results = mutableListOf<MediaItem>()

        if (tmdbConfig.isConfigured && (type == null || type == ContentType.MOVIE || type == ContentType.SERIES)) {
            tmdbProvider.search(query, type).getOrNull()?.let { results.addAll(it) }
        }

        if (m3uProvider.isLoaded && (type == null || type == ContentType.LIVE_TV || type == ContentType.SPORTS)) {
            m3uProvider.search(query, type).getOrNull()?.let { results.addAll(it) }
        }

        if (results.isEmpty()) {
            return demoProvider.search(query, type)
        }
        return Result.success(results)
    }

    override suspend fun getDetails(mediaId: String, type: ContentType): Result<MediaItem> {
        if (mediaId.startsWith("tmdb_")) {
            val rawId = mediaId.removePrefix("tmdb_m_").removePrefix("tmdb_tv_")
            return tmdbProvider.getDetails(rawId, type)
        }
        if (m3uProvider.isLoaded && mediaId.startsWith("m3u_")) {
            return m3uProvider.getDetails(mediaId, type)
        }
        return demoProvider.getDetails(mediaId, type)
    }

    override suspend fun getGenres(type: ContentType): Result<List<String>> {
        if (tmdbConfig.isConfigured && (type == ContentType.MOVIE || type == ContentType.SERIES)) {
            val tmdbResult = tmdbProvider.getGenres(type)
            if (tmdbResult.isSuccess && tmdbResult.getOrNull()?.isNotEmpty() == true) {
                return tmdbResult
            }
        }
        return Result.success(listOf("Action", "Comedy", "Drama", "Sci-Fi", "Animation"))
    }

    override suspend fun getByGenre(genre: String, type: ContentType): Result<List<MediaItem>> {
        if (tmdbConfig.isConfigured && (type == ContentType.MOVIE || type == ContentType.SERIES)) {
            val tmdbResult = tmdbProvider.getByGenre(genre, type)
            if (tmdbResult.isSuccess && tmdbResult.getOrNull()?.isNotEmpty() == true) {
                return tmdbResult
            }
        }
        return demoProvider.getByCategory(genre, type)
    }
}

class CompositeStreamProvider(
    private val m3uProvider: M3uStreamProvider,
    private val demoProvider: DemoStreamProvider = DemoStreamProvider()
) : StreamProvider {

    override val id: String = "composite_stream"
    override val name: String = "Composite Playback Stream Engine"
    override val supportedTypes: Set<ContentType> = ContentType.entries.toSet()

    override suspend fun canHandle(mediaItem: MediaItem): Boolean {
        return m3uProvider.canHandle(mediaItem) || demoProvider.canHandle(mediaItem)
    }

    override suspend fun resolveStream(
        mediaItem: MediaItem,
        season: Int?,
        episode: Int?
    ): StreamResult {
        if (m3uProvider.canHandle(mediaItem)) {
            val result = m3uProvider.resolveStream(mediaItem, season, episode)
            if (result is StreamResult.Success) {
                return result
            }
        }
        return demoProvider.resolveStream(mediaItem, season, episode)
    }
}
