package com.streamtv.core.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class TvPlayerController(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekForwardIncrementMs(10_000)
            .setSeekBackIncrementMs(10_000)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val isBuffering = playbackState == Player.STATE_BUFFERING
                        _playerState.update { current ->
                            current.copy(
                                isBuffering = isBuffering,
                                durationMs = if (duration != C.TIME_UNSET) duration else 0L,
                                isLive = isCurrentMediaItemLive
                            )
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) {
                            startProgressTracking()
                        } else {
                            stopProgressTracking()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val userFriendlyMessage = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "Network connection failed. Please check your internet connection."
                            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                                "Unable to parse media stream. Stream format may be invalid."
                            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                                "Hardware decoder could not process this video stream."
                            else -> "Playback error: ${error.localizedMessage ?: "Unknown error"}"
                        }
                        _playerState.update {
                            it.copy(
                                isBuffering = false,
                                isPlaying = false,
                                errorMessage = userFriendlyMessage,
                                error = error
                            )
                        }
                    }
                })
            }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun playStream(mediaItem: MediaItem, streamResult: StreamResult.Success) {
        val player = exoPlayer ?: return

        _playerState.update {
            it.copy(
                errorMessage = null,
                error = null,
                title = mediaItem.title,
                subtitle = mediaItem.episodeTitle ?: mediaItem.releaseYear ?: ""
            )
        }

        val exoMediaItem = ExoMediaItem.Builder()
            .setUri(Uri.parse(streamResult.url))
            .setMediaId(mediaItem.id)
            .build()

        player.setMediaItem(exoMediaItem)
        player.prepare()
        player.play()
    }

    fun setError(message: String) {
        _playerState.update {
            it.copy(
                isBuffering = false,
                isPlaying = false,
                errorMessage = message
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekForward(ms: Long = 10_000) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + ms).coerceAtMost(player.duration.coerceAtLeast(0))
        player.seekTo(newPos)
    }

    fun seekBackward(ms: Long = 10_000) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - ms).coerceAtLeast(0)
        player.seekTo(newPos)
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    _playerState.update {
                        it.copy(
                            currentPositionMs = player.currentPosition.coerceAtLeast(0),
                            durationMs = if (player.duration != C.TIME_UNSET) player.duration else 0L
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracking()
        exoPlayer?.release()
        exoPlayer = null
    }
}
