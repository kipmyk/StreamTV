package com.streamtv.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.streamtv.app.ui.theme.TvFocusBorder
import com.streamtv.app.ui.theme.TvSurfaceVariant
import com.streamtv.app.ui.theme.TvTextPrimary
import com.streamtv.app.ui.theme.TvTextSecondary
import com.streamtv.core.model.MediaItem

@Composable
fun TvMediaCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1.0f, label = "cardScale")

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(180.dp)
            .padding(8.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TvSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Poster / Backdrop Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(Color.DarkGray)
            ) {
                if (!mediaItem.posterUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = mediaItem.posterUrl,
                        contentDescription = mediaItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Type or Rating Badge
                if (mediaItem.rating != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ ${mediaItem.rating}",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & Subtitle details
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = mediaItem.title,
                    color = TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mediaItem.releaseYear ?: mediaItem.contentType.name,
                    color = TvTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
