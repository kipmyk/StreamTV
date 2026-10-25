package com.streamtv.app.ui.series

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
fun SeriesScreen(
    metadataProvider: MetadataProvider,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var trendingSeries by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var popularSeries by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var topRatedSeries by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val loadSeries: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val trendingResult = metadataProvider.getTrending(ContentType.SERIES)
                val popularResult = metadataProvider.getByCategory("popular", ContentType.SERIES)
                val topRatedResult = metadataProvider.getByCategory("top_rated", ContentType.SERIES)

                if (trendingResult.isFailure && popularResult.isFailure) {
                    val ex = trendingResult.exceptionOrNull() ?: popularResult.exceptionOrNull()
                    errorMessage = ex?.message ?: "Failed to load TV series."
                } else {
                    trendingSeries = trendingResult.getOrDefault(emptyList())
                    popularSeries = popularResult.getOrDefault(emptyList())
                    topRatedSeries = topRatedResult.getOrDefault(emptyList())
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unexpected error loading TV series."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(metadataProvider) {
        loadSeries()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                TvLoadingIndicator(message = "Discovering TV series...")
            }
            errorMessage != null -> {
                TvErrorState(
                    title = "Series Catalog Error",
                    errorMessage = errorMessage!!,
                    onRetry = loadSeries
                )
            }
            trendingSeries.isEmpty() && popularSeries.isEmpty() && topRatedSeries.isEmpty() -> {
                TvEmptyState(
                    title = "No Series Found",
                    subtitle = "The provider returned no TV series."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                ) {
                    if (trendingSeries.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "🔥 Trending Series",
                                items = trendingSeries,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (popularSeries.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "📺 Popular TV Shows",
                                items = popularSeries,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    if (topRatedSeries.isNotEmpty()) {
                        item {
                            TvCategoryRow(
                                title = "⭐ Top Rated Series",
                                items = topRatedSeries,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
