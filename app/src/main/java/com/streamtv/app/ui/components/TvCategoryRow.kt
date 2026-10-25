package com.streamtv.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamtv.app.ui.theme.TvTextPrimary
import com.streamtv.core.model.MediaItem

@Composable
fun TvCategoryRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TvTextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                TvMediaCard(
                    mediaItem = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
