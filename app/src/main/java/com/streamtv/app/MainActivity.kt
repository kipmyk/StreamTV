package com.streamtv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.streamtv.app.navigation.AppNavigation
import com.streamtv.app.provider.CompositeMetadataProvider
import com.streamtv.app.provider.CompositeStreamProvider
import com.streamtv.app.ui.theme.StreamTvTheme
import com.streamtv.providers.m3u.M3uStreamProvider
import com.streamtv.providers.tmdb.TmdbConfig
import com.streamtv.providers.tmdb.TmdbMetadataProvider

class MainActivity : ComponentActivity() {

    private val tmdbConfig = TmdbConfig()
    private val tmdbProvider = TmdbMetadataProvider(config = tmdbConfig)
    private val m3uProvider = M3uStreamProvider()

    private val metadataProvider = CompositeMetadataProvider(
        tmdbProvider = tmdbProvider,
        m3uProvider = m3uProvider,
        tmdbConfig = tmdbConfig
    )

    private val streamProvider = CompositeStreamProvider(
        m3uProvider = m3uProvider
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StreamTvTheme {
                AppNavigation(
                    metadataProvider = metadataProvider,
                    streamProvider = streamProvider,
                    tmdbConfig = tmdbConfig,
                    m3uProvider = m3uProvider
                )
            }
        }
    }
}
