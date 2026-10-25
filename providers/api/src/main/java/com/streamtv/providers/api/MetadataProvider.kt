package com.streamtv.providers.api

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem

/**
 * Interface responsible for fetching metadata, categories, trending titles,
 * and search results. It has NO responsibility for streaming playback.
 */
interface MetadataProvider {
    val id: String
    val name: String
    val supportedTypes: Set<ContentType>

    suspend fun getTrending(type: ContentType): Result<List<MediaItem>>
    suspend fun getByCategory(category: String, type: ContentType): Result<List<MediaItem>>
    suspend fun search(query: String, type: ContentType? = null): Result<List<MediaItem>>
    suspend fun getDetails(mediaId: String, type: ContentType): Result<MediaItem>
    suspend fun getGenres(type: ContentType): Result<List<String>> = Result.success(emptyList())
    suspend fun getByGenre(genre: String, type: ContentType): Result<List<MediaItem>> = Result.success(emptyList())
}
