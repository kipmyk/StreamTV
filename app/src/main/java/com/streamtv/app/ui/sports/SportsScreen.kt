package com.streamtv.app.ui.sports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamtv.app.ui.components.TvCategoryRow
import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.providers.api.MetadataProvider

@Composable
fun SportsScreen(
    metadataProvider: MetadataProvider,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var sports by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        sports = metadataProvider.getTrending(ContentType.SPORTS).getOrDefault(emptyList())
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            item {
                TvCategoryRow(
                    title = "⚽ Live & Upcoming Sports",
                    items = sports,
                    onItemClick = onItemClick
                )
            }
        }
    }
}
