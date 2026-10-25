package com.streamtv.providers.tmdb

class TmdbConfig(
    apiKey: String? = null,
    val apiBaseUrl: String = "https://api.themoviedb.org/3",
    val imageBaseUrl: String = "https://image.tmdb.org/t/p/w500",
    val backdropBaseUrl: String = "https://image.tmdb.org/t/p/original"
) {
    var apiKey: String? = apiKey ?: resolveApiKey()

    val isConfigured: Boolean
        get() = !apiKey.isNullOrBlank()

    private fun resolveApiKey(): String? {
        val envKey = System.getenv("TMDB_API_KEY")
        if (!envKey.isNullOrBlank()) return envKey

        val propKey = System.getProperty("tmdb.api.key")
        if (!propKey.isNullOrBlank()) return propKey

        return null
    }
}
