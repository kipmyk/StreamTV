package com.streamtv.providers.tmdb

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbResponseParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testParseMovieTrendingResponse() {
        val sampleJson = """
            {
              "page": 1,
              "results": [
                {
                  "id": 157336,
                  "title": "Interstellar",
                  "overview": "The adventures of a group of explorers...",
                  "poster_path": "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                  "backdrop_path": "/rAiYTgggrikMoPVCRvdAVla6uE9.jpg",
                  "release_date": "2014-11-05",
                  "vote_average": 8.433,
                  "genre_ids": [12, 18, 878]
                }
              ],
              "total_pages": 10,
              "total_results": 200
            }
        """.trimIndent()

        val response = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(sampleJson)
        assertEquals(1, response.page)
        assertEquals(1, response.results.size)
        val movie = response.results.first()
        assertEquals(157336L, movie.id)
        assertEquals("Interstellar", movie.title)
        assertEquals("2014-11-05", movie.releaseDate)
        assertEquals(8.433, movie.voteAverage ?: 0.0, 0.001)
        assertEquals(listOf(12, 18, 878), movie.genreIds)
    }

    @Test
    fun testParseTvSeriesResponse() {
        val sampleJson = """
            {
              "page": 1,
              "results": [
                {
                  "id": 1399,
                  "name": "Game of Thrones",
                  "overview": "Seven noble families fight for control of the mythical land of Westeros.",
                  "poster_path": "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
                  "first_air_date": "2011-04-17",
                  "vote_average": 8.452
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<TmdbPaginatedResponse<TmdbTv>>(sampleJson)
        assertEquals(1, response.results.size)
        val tv = response.results.first()
        assertEquals(1399L, tv.id)
        assertEquals("Game of Thrones", tv.name)
        assertEquals("2011-04-17", tv.firstAirDate)
    }

    @Test
    fun testParseGenreResponse() {
        val sampleJson = """
            {
              "genres": [
                { "id": 28, "name": "Action" },
                { "id": 12, "name": "Adventure" },
                { "id": 16, "name": "Animation" }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<TmdbGenreResponse>(sampleJson)
        assertEquals(3, response.genres.size)
        assertEquals("Action", response.genres[0].name)
        assertEquals(28, response.genres[0].id)
    }

    @Test
    fun testParseEmptyResults() {
        val sampleJson = """
            {
              "page": 1,
              "results": [],
              "total_pages": 0,
              "total_results": 0
            }
        """.trimIndent()

        val response = json.decodeFromString<TmdbPaginatedResponse<TmdbMovie>>(sampleJson)
        assertTrue(response.results.isEmpty())
        assertEquals(0, response.totalResults)
    }

    @Test
    fun testParseApiErrorResponse() {
        val errorJson = """
            {
              "status_code": 7,
              "status_message": "Invalid API key: You must be granted a valid key.",
              "success": false
            }
        """.trimIndent()

        val error = json.decodeFromString<TmdbErrorResponse>(errorJson)
        assertEquals(7, error.statusCode)
        assertNotNull(error.statusMessage)
        assertEquals(false, error.success)
    }
}
