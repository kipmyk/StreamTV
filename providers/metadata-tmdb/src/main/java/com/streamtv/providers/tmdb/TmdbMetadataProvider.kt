package com.streamtv.providers.tmdb

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.network.NetworkClient
import com.streamtv.providers.api.MetadataProvider
import kotlinx.serialization.json.Json
import java.net.URLEncoder

class TmdbMetadataProvider(
    private val config: TmdbConfig = TmdbConfig(),
    private val networkClient: NetworkClient = NetworkClient()
) : MetadataProvider {

    override val id: String = "provider_tmdb"
    override val name: String = "TheMovieDatabase (TMDB)"
    override val supportedTypes: Set<ContentType> = setOf(ContentType.MOVIE, ContentType.SERIES)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var genreCacheMovie: Map<String, Int>? = null
    private var genreCacheTv: Map<String, Int>? = null

    private fun checkConfigured(): Result<Unit> {
        return if (config.isConfigured) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("TMDB API key is not configured. Please supply a key via settings or local.properties."))
        }
    }

    override suspend fun getTrending(type: ContentType): Result<List<MediaItem>> {
        checkConfigured().onFailure { return Result.failure(it) }

        val endpoint = when (type) {
            ContentType.MOVIE -> "${config.apiBaseUrl}/trending/movie/week?api_key=${config.apiKey}"
            ContentType.SERIES -> "${config.apiBaseUrl}/trending/tv/week?api_key=${config.apiKey}"
            else -> return Result.success(emptyList())
        }

        return networkClient.get(endpoint).mapCatching { responseBody ->
            when (type) {
                ContentType.MOVIE -> {
                    val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(responseBody)
                    parsed.results.map { it.toMediaItem(config) }
                }
                ContentType.SERIES -> {
                    val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbTv>>(responseBody)
                    parsed.results.map { it.toMediaItem(config) }
                }
                else -> emptyList()
            }
        }
    }

    override suspend fun getByCategory(category: String, type: ContentType): Result<List<MediaItem>> {
        checkConfigured().onFailure { return Result.failure(it) }

        val path = when (category.lowercase()) {
            "top rated", "top_rated" -> if (type == ContentType.MOVIE) "movie/top_rated" else "tv/top_rated"
            "upcoming" -> if (type == ContentType.MOVIE) "movie/upcoming" else "tv/on_the_air"
            else -> if (type == ContentType.MOVIE) "movie/popular" else "tv/popular"
        }

        val endpoint = "${config.apiBaseUrl}/$path?api_key=${config.apiKey}"

        return networkClient.get(endpoint).mapCatching { responseBody ->
            when (type) {
                ContentType.MOVIE -> {
                    val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(responseBody)
                    parsed.results.map { it.toMediaItem(config, category) }
                }
                ContentType.SERIES -> {
                    val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbTv>>(responseBody)
                    parsed.results.map { it.toMediaItem(config, category) }
                }
                else -> emptyList()
            }
        }
    }

    override suspend fun search(query: String, type: ContentType?): Result<List<MediaItem>> {
        checkConfigured().onFailure { return Result.failure(it) }
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")

        val targetType = type ?: ContentType.MOVIE
        val path = if (targetType == ContentType.SERIES) "search/tv" else "search/movie"
        val endpoint = "${config.apiBaseUrl}/$path?api_key=${config.apiKey}&query=$encodedQuery"

        return networkClient.get(endpoint).mapCatching { responseBody ->
            if (targetType == ContentType.SERIES) {
                val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbTv>>(responseBody)
                parsed.results.map { it.toMediaItem(config, "Search Results") }
            } else {
                val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(responseBody)
                parsed.results.map { it.toMediaItem(config, "Search Results") }
            }
        }
    }

    override suspend fun getDetails(mediaId: String, type: ContentType): Result<MediaItem> {
        checkConfigured().onFailure { return Result.failure(it) }

        val path = if (type == ContentType.SERIES) "tv/$mediaId" else "movie/$mediaId"
        val endpoint = "${config.apiBaseUrl}/$path?api_key=${config.apiKey}"

        return networkClient.get(endpoint).mapCatching { responseBody ->
            if (type == ContentType.SERIES) {
                val parsed = json.decodeFromString<TmdbTvDetails>(responseBody)
                parsed.toMediaItem(config)
            } else {
                val parsed = json.decodeFromString<TmdbMovieDetails>(responseBody)
                parsed.toMediaItem(config)
            }
        }
    }

    override suspend fun getGenres(type: ContentType): Result<List<String>> {
        checkConfigured().onFailure { return Result.failure(it) }

        val path = if (type == ContentType.SERIES) "genre/tv/list" else "genre/movie/list"
        val endpoint = "${config.apiBaseUrl}/$path?api_key=${config.apiKey}"

        return networkClient.get(endpoint).mapCatching { responseBody ->
            val parsed = json.decodeFromString<TmdbGenreResponse>(responseBody)
            val genreMap = parsed.genres.associate { it.name to it.id }
            if (type == ContentType.SERIES) {
                genreCacheTv = genreMap
            } else {
                genreCacheMovie = genreMap
            }
            parsed.genres.map { it.name }
        }
    }

    override suspend fun getByGenre(genre: String, type: ContentType): Result<List<MediaItem>> {
        checkConfigured().onFailure { return Result.failure(it) }

        val cache = if (type == ContentType.SERIES) genreCacheTv else genreCacheMovie
        val genreId = cache?.get(genre) ?: run {
            getGenres(type).getOrNull()
            (if (type == ContentType.SERIES) genreCacheTv else genreCacheMovie)?.get(genre)
        }

        if (genreId == null) {
            return Result.failure(IllegalArgumentException("Unknown genre: $genre"))
        }

        val path = if (type == ContentType.SERIES) "discover/tv" else "discover/movie"
        val endpoint = "${config.apiBaseUrl}/$path?api_key=${config.apiKey}&with_genres=$genreId"

        return networkClient.get(endpoint).mapCatching { responseBody ->
            if (type == ContentType.SERIES) {
                val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbTv>>(responseBody)
                parsed.results.map { it.toMediaItem(config, genre) }
            } else {
                val parsed = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(responseBody)
                parsed.results.map { it.toMediaItem(config, genre) }
            }
        }
    }
}

// Mapper extensions
private fun TmdbMovie.toMediaItem(config: TmdbConfig, categoryName: String = "Movies"): MediaItem {
    return MediaItem(
        id = "tmdb_m_$id",
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { "${config.imageBaseUrl}$it" },
        backdropUrl = backdropPath?.let { "${config.backdropBaseUrl}$it" },
        contentType = ContentType.MOVIE,
        releaseYear = releaseDate?.take(4),
        rating = voteAverage?.let { Math.round(it * 10.0) / 10.0 },
        providerId = "provider_tmdb",
        category = categoryName
    )
}

private fun TmdbTv.toMediaItem(config: TmdbConfig, categoryName: String = "Series"): MediaItem {
    return MediaItem(
        id = "tmdb_tv_$id",
        title = name,
        overview = overview,
        posterUrl = posterPath?.let { "${config.imageBaseUrl}$it" },
        backdropUrl = backdropPath?.let { "${config.backdropBaseUrl}$it" },
        contentType = ContentType.SERIES,
        releaseYear = firstAirDate?.take(4),
        rating = voteAverage?.let { Math.round(it * 10.0) / 10.0 },
        providerId = "provider_tmdb",
        category = categoryName
    )
}

private fun TmdbMovieDetails.toMediaItem(config: TmdbConfig): MediaItem {
    return MediaItem(
        id = "tmdb_m_$id",
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { "${config.imageBaseUrl}$it" },
        backdropUrl = backdropPath?.let { "${config.backdropBaseUrl}$it" },
        contentType = ContentType.MOVIE,
        releaseYear = releaseDate?.take(4),
        rating = voteAverage?.let { Math.round(it * 10.0) / 10.0 },
        providerId = "provider_tmdb",
        category = tagline ?: "Details",
        genres = genres.map { it.name }
    )
}

private fun TmdbTvDetails.toMediaItem(config: TmdbConfig): MediaItem {
    return MediaItem(
        id = "tmdb_tv_$id",
        title = name,
        overview = overview,
        posterUrl = posterPath?.let { "${config.imageBaseUrl}$it" },
        backdropUrl = backdropPath?.let { "${config.backdropBaseUrl}$it" },
        contentType = ContentType.SERIES,
        releaseYear = firstAirDate?.take(4),
        rating = voteAverage?.let { Math.round(it * 10.0) / 10.0 },
        providerId = "provider_tmdb",
        category = "Details",
        seasonNumber = numberOfSeasons,
        episodeNumber = numberOfEpisodes,
        genres = genres.map { it.name }
    )
}
