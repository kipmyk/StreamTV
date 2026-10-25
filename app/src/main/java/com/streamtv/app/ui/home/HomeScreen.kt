package com.streamtv.app.ui.home

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
fun HomeScreen(
    metadataProvider: MetadataProvider,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var trendingMovies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var trendingSeries by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var liveChannels by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var liveSports by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val loadData: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val movieResult = metadataProvider.getTrending(ContentType.MOVIE)
                val seriesResult = metadataProvider.getTrending(ContentType.SERIES)
                val liveResult = metadataProvider.getTrending(ContentType.LIVE_TV)
                val sportsResult = metadataProvider.getTrending(ContentType.SPORTS)

                if (movieResult.isFailure && seriesResult.isFailure && liveResult.isFailure) {
                    val ex = movieResult.exceptionOrNull() ?: seriesResult.exceptionOrNull()
                    errorMessage = ex?.message ?: "Unable to load media content from provider."
                } else {
                    trendingMovies = movieResult.getOrDefault(emptyList())
                    trendingSeries = seriesResult.getOrDefault(emptyList())
                    liveChannels = liveResult.getOrDefault(emptyList())
                    liveSports = sportsResult.getOrDefault(emptyList())
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unexpected error loading content."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(metadataProvider) {
        loadData()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                TvLoadingIndicator(message = "Loading home catalog...")
            }
            errorMessage != null -> {
                TvErrorState(
                    errorMessage = errorMessage!!,
                    onRetry = loadData
                )
            }
            trendingMovies.isEmpty() && trendingSeries.isEmpty() && liveChannels.isEmpty() && liveSports.isEmpty() -> {
                TvEmptyState(
                    title = "Catalog Empty",
                    subtitle = "No content returned from ${metadataProvider.name}."
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
                                title = "🔥 Featured Movies",
                                items = trendingMovies,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (trendingSeries.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "📺 Popular Series",
                                items = trendingSeries,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (liveChannels.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "📡 Live Channels",
                                items = liveChannels,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (liveSports.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "⚽ Live Sports",
                                items = liveSports,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
