package com.streamtv.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    var isButtonFocused by remember { mutableStateOf(false) }

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
                        text = "API Key: $masked (Source: local.properties / env)",
                        fontSize = 13.sp,
                        color = TvTextSecondary
                    )
                } else {
                    Text(
                        text = "To enable real TMDB movie & series discovery, add 'tmdb.api.key=YOUR_KEY' in local.properties or export TMDB_API_KEY.",
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
                        text = "M3U Live Playlist Parser",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (m3uProvider.isLoaded) "● ${m3uProvider.totalChannels} CHANNELS" else "○ EMPTY",
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

                Surface(
                    onClick = {
                        coroutineScope.launch {
                            playlistStatus = "Loading sample public test channels..."
                            // Load sample verified public test broadcast M3U playlist
                            val sampleM3u = """
                                #EXTM3U x-tvg-url="https://iptv-org.github.io/epg/guides/us/tvguide.com.epg.xml"
                                #EXTINF:-1 tvg-id="akamai.live" tvg-name="Akamai Live News" group-title="News",Akamai Live News HLS
                                https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8
                                #EXTINF:-1 tvg-id="unified.sports" tvg-name="Action Sports" group-title="Sports",Unified Sports Action 1080p
                                https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8
                                #EXTINF:-1 tvg-id="mux.animation" tvg-name="Big Buck Bunny TV" group-title="Animation",Big Buck Bunny 24/7
                                https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8
                            """.trimIndent()
                            val tempFile = java.io.File.createTempFile("sample_test_playlist", ".m3u")
                            tempFile.writeText(sampleM3u)
                            val res = m3uProvider.loadPlaylist(tempFile.absolutePath)
                            if (res.isSuccess) {
                                playlistStatus = "Successfully loaded ${res.getOrNull()} channels from test M3U playlist."
                                onPlaylistLoaded()
                            } else {
                                playlistStatus = "Failed to load: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .onFocusChanged { isButtonFocused = it.isFocused }
                        .border(
                            width = if (isButtonFocused) 2.dp else 0.dp,
                            color = if (isButtonFocused) TvFocusBorder else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = TvPrimary,
                        focusedContainerColor = TvPrimary
                    )
                ) {
                    Text(
                        text = "Load Sample Verified Playlist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
