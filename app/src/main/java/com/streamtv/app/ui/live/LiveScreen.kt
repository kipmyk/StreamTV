package com.streamtv.app.ui.live

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun LiveScreen(
    metadataProvider: MetadataProvider,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var channels by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var groupedChannels by remember { mutableStateOf<Map<String, List<MediaItem>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val loadChannels: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val result = metadataProvider.getTrending(ContentType.LIVE_TV)
                if (result.isSuccess) {
                    val list = result.getOrDefault(emptyList())
                    channels = list
                    groupedChannels = list.groupBy { it.groupTitle ?: "General" }
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load live channels."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unexpected error loading live channels."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(metadataProvider) {
        loadChannels()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                TvLoadingIndicator(message = "Parsing live TV channels...")
            }
            errorMessage != null -> {
                TvErrorState(
                    title = "Live TV Error",
                    errorMessage = errorMessage!!,
                    onRetry = loadChannels
                )
            }
            channels.isEmpty() -> {
                TvEmptyState(
                    title = "No Live Channels Found",
                    subtitle = "Configure an M3U playlist URL or import a playlist file in Settings."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                ) {
                    groupedChannels.forEach { (groupName, groupItems) ->
                        item(key = groupName) {
                            TvCategoryRow(
                                title = "📡 $groupName",
                                items = groupItems,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}
