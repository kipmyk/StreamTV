package com.streamtv.providers.demo

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.providers.api.MetadataProvider

class DemoMetadataProvider : MetadataProvider {
    override val id: String = "demo_metadata"
    override val name: String = "Demo Test Catalog"
    override val supportedTypes: Set<ContentType> = ContentType.entries.toSet()

    private val sampleItems = listOf(
        MediaItem(
            id = "demo_movie_1",
            title = "Big Buck Bunny (HLS Test)",
            overview = "A large and lovable rabbit deals with bullying forest creatures in this classic open-source test film.",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1280&q=80",
            contentType = ContentType.MOVIE,
            releaseYear = "2008",
            rating = 8.5,
            category = "Featured Movies"
        ),
        MediaItem(
            id = "demo_movie_2",
            title = "Tears of Steel (Sci-Fi HLS)",
            overview = "Set in a dystopian future where a group of warriors and scientists gather at the Oude Kerk in Amsterdam.",
            posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=500&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=1280&q=80",
            contentType = ContentType.MOVIE,
            releaseYear = "2012",
            rating = 7.9,
            category = "Sci-Fi"
        ),
        MediaItem(
            id = "demo_series_1",
            title = "Cosmos Exploration (Ep 1)",
            overview = "A breathtaking journey through deep space, distant galaxies, and astronomical phenomena.",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280&q=80",
            contentType = ContentType.SERIES,
            releaseYear = "2024",
            seasonNumber = 1,
            episodeNumber = 1,
            episodeTitle = "The Edge of Unknown",
            category = "Trending Series"
        ),
        MediaItem(
            id = "demo_live_1",
            title = "Global News 24/7 (Live HLS)",
            overview = "Live broadcast streaming test feed with multi-bitrate HLS adaptive playback.",
            posterUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=500&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=1280&q=80",
            contentType = ContentType.LIVE_TV,
            category = "News & Info"
        ),
        MediaItem(
            id = "demo_sports_1",
            title = "Red Bull Extreme Sports (Action Test)",
            overview = "High adrenaline downhill mountain biking, rally racing, and aerial acrobatics.",
            posterUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=500&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=1280&q=80",
            contentType = ContentType.SPORTS,
            category = "Live Sports"
        )
    )

    override suspend fun getTrending(type: ContentType): Result<List<MediaItem>> {
        val filtered = sampleItems.filter { it.contentType == type }
        return Result.success(if (filtered.isNotEmpty()) filtered else sampleItems)
    }

    override suspend fun getByCategory(category: String, type: ContentType): Result<List<MediaItem>> {
        val filtered = sampleItems.filter { it.contentType == type }
        return Result.success(filtered)
    }

    override suspend fun search(query: String, type: ContentType?): Result<List<MediaItem>> {
        val filtered = sampleItems.filter {
            (type == null || it.contentType == type) &&
            it.title.contains(query, ignoreCase = true)
        }
        return Result.success(filtered)
    }

    override suspend fun getDetails(mediaId: String, type: ContentType): Result<MediaItem> {
        val item = sampleItems.find { it.id == mediaId }
            ?: return Result.failure(NoSuchElementException("Item $mediaId not found"))
        return Result.success(item)
    }
}
