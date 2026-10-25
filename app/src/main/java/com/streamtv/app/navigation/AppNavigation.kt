package com.streamtv.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.streamtv.app.ui.components.NavDestination
import com.streamtv.app.ui.components.TvSideBar
import com.streamtv.app.ui.home.HomeScreen
import com.streamtv.app.ui.live.LiveScreen
import com.streamtv.app.ui.movies.MoviesScreen
import com.streamtv.app.ui.player.PlayerScreen
import com.streamtv.app.ui.series.SeriesScreen
import com.streamtv.app.ui.settings.SettingsScreen
import com.streamtv.app.ui.sports.SportsScreen
import com.streamtv.app.ui.theme.TvBackground
import com.streamtv.core.model.MediaItem
import com.streamtv.providers.api.MetadataProvider
import com.streamtv.providers.api.StreamProvider

@Composable
fun AppNavigation(
    metadataProvider: MetadataProvider,
    streamProvider: StreamProvider,
    tmdbConfig: com.streamtv.providers.tmdb.TmdbConfig,
    m3uProvider: com.streamtv.providers.m3u.M3uStreamProvider,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var playingItem by remember { mutableStateOf<MediaItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        // If an item is active, render the fullscreen PlayerScreen
        if (playingItem != null) {
            PlayerScreen(
                mediaItem = playingItem!!,
                streamProvider = streamProvider,
                onBack = { playingItem = null }
            )
        } else {
            // Main TV Shell with Sidebar navigation and Content Area
            Row(modifier = Modifier.fillMaxSize()) {
                TvSideBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { currentDestination = it }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentDestination) {
                        NavDestination.HOME -> HomeScreen(
                            metadataProvider = metadataProvider,
                            onItemClick = { playingItem = it }
                        )
                        NavDestination.MOVIES -> MoviesScreen(
                            metadataProvider = metadataProvider,
                            onItemClick = { playingItem = it }
                        )
                        NavDestination.SERIES -> SeriesScreen(
                            metadataProvider = metadataProvider,
                            onItemClick = { playingItem = it }
                        )
                        NavDestination.LIVE -> LiveScreen(
                            metadataProvider = metadataProvider,
                            onItemClick = { playingItem = it }
                        )
                        NavDestination.SPORTS -> SportsScreen(
                            metadataProvider = metadataProvider,
                            onItemClick = { playingItem = it }
                        )
                        NavDestination.SETTINGS -> SettingsScreen(
                            tmdbConfig = tmdbConfig,
                            m3uProvider = m3uProvider
                        )
                    }
                }
            }
        }
    }
}
