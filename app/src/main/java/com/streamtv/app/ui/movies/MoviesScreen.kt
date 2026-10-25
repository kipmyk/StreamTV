package com.streamtv.app.ui.movies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamtv.app.ui.components.TvCategoryRow
import com.streamtv.app.ui.components.TvEmptyState
import com.streamtv.app.ui.components.TvErrorState
import com.streamtv.app.ui.components.TvLoadingIndicator
import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.providers.api.MetadataProvider
import kotlinx.coroutines.launch

@Composable
fun MoviesScreen(
    metadataProvider: MetadataProvider,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var trendingMovies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var topRatedMovies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val loadMovies: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val trendingResult = metadataProvider.getTrending(ContentType.MOVIE)
                val popularResult = metadataProvider.getByCategory("popular", ContentType.MOVIE)
                val topRatedResult = metadataProvider.getByCategory("top_rated", ContentType.MOVIE)

                if (trendingResult.isFailure && popularResult.isFailure) {
                    val ex = trendingResult.exceptionOrNull() ?: popularResult.exceptionOrNull()
                    errorMessage = ex?.message ?: "Failed to load movies."
                } else {
                    trendingMovies = trendingResult.getOrDefault(emptyList())
                    popularMovies = popularResult.getOrDefault(emptyList())
                    topRatedMovies = topRatedResult.getOrDefault(emptyList())
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unexpected error loading movies."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(metadataProvider) {
        loadMovies()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                TvLoadingIndicator(message = "Discovering movies...")
            }
            errorMessage != null -> {
                TvErrorState(
                    title = "Movie Catalog Error",
                    errorMessage = errorMessage!!,
                    onRetry = loadMovies
                )
            }
            trendingMovies.isEmpty() && popularMovies.isEmpty() && topRatedMovies.isEmpty() -> {
                TvEmptyState(
                    title = "No Movies Found",
                    subtitle = "The movie provider returned no results."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                ) {
                    if (trendingMovies.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "🔥 Trending This Week",
                                items = trendingMovies,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (popularMovies.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "🎬 Popular Movies",
                                items = popularMovies,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (topRatedMovies.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "⭐ Highest Rated",
                                items = topRatedMovies,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
