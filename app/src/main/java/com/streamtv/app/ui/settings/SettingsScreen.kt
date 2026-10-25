package com.streamtv.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.streamtv.app.ui.theme.TvFocusBorder
import com.streamtv.app.ui.theme.TvPrimary
import com.streamtv.app.ui.theme.TvSurfaceVariant
import com.streamtv.app.ui.theme.TvTextPrimary
import com.streamtv.app.ui.theme.TvTextSecondary
import com.streamtv.providers.m3u.FreeIptvFeeds
import com.streamtv.providers.m3u.M3uStreamProvider
import com.streamtv.providers.tmdb.TmdbConfig
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    tmdbConfig: TmdbConfig,
    m3uProvider: M3uStreamProvider,
    onPlaylistLoaded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var playlistStatus by remember {
        mutableStateOf(
            if (m3uProvider.isLoaded) "Loaded ${m3uProvider.totalChannels} channels from ${m3uProvider.currentSource}"
            else "No playlist loaded (Defaulting to test demo channels)"
        )
    }

    val loadRemoteFeed: (String, String) -> Unit = { feedName, feedUrl ->
        coroutineScope.launch {
            playlistStatus = "Loading $feedName..."
            val res = m3uProvider.loadPlaylist(feedUrl)
            if (res.isSuccess) {
                playlistStatus = "Successfully loaded ${res.getOrNull()} channels from $feedName."
                onPlaylistLoaded()
            } else {
                playlistStatus = "Failed to load $feedName: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Settings & Providers",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TvTextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TMDB Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TvSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TheMovieDatabase (TMDB)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (tmdbConfig.isConfigured) "● ACTIVE" else "○ NOT CONFIGURED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tmdbConfig.isConfigured) Color(0xFF00E676) else Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (tmdbConfig.isConfigured) {
                    val masked = tmdbConfig.apiKey?.let {
                        if (it.length > 8) "${it.take(4)}...${it.takeLast(4)}" else "****"
                    } ?: "****"
                    Text(
                        text = "API Key: $masked (Configured in local.properties)",
                        fontSize = 13.sp,
                        color = TvTextSecondary
                    )
                } else {
                    Text(
                        text = "To enable TMDB discovery, add 'tmdb.api.key=YOUR_KEY' in local.properties.",
                        fontSize = 13.sp,
                        color = TvTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // M3U Playlist Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TvSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Free Live Streaming TV Feeds",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (m3uProvider.isLoaded) "● ${m3uProvider.totalChannels} CHANNELS" else "○ READY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (m3uProvider.isLoaded) Color(0xFF00E676) else Color(0xFFFF9800)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playlistStatus,
                    fontSize = 13.sp,
                    color = TvTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select a Free Public Live TV Source:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TvTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeedButton(
                            title = "📰 Global News (Live)",
                            onClick = { loadRemoteFeed("Free Global News", FreeIptvFeeds.GLOBAL_NEWS) }
                        )
                        FeedButton(
                            title = "⚽ Live Sports (HLS)",
                            onClick = { loadRemoteFeed("Free Live Sports", FreeIptvFeeds.SPORTS) }
                        )
                        FeedButton(
                            title = "🎬 Movie Channels (24/7)",
                            onClick = { loadRemoteFeed("Free Movies & Cinema", FreeIptvFeeds.MOVIES) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeedButton(
                            title = "🚀 Science & Tech TV",
                            onClick = { loadRemoteFeed("Science & Space TV", "https://iptv-org.github.io/iptv/categories/science.m3u") }
                        )
                        FeedButton(
                            title = "🇬🇧 English Free-To-Air",
                            onClick = { loadRemoteFeed("English Channels", FreeIptvFeeds.ENGLISH_ALL) }
                        )
                        FeedButton(
                            title = "⚡ Verified 24/7 Feeds",
                            onClick = {
                                m3uProvider.loadDefaultVerifiedFeeds()
                                playlistStatus = "Loaded ${m3uProvider.totalChannels} verified 24/7 test broadcast channels."
                                onPlaylistLoaded()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TvPrimary,
            focusedContainerColor = TvPrimary
        )
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}
