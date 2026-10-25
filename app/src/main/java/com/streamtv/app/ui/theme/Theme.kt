package com.streamtv.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val TvBackground = Color(0xFF0C0D14)
val TvSurface = Color(0xFF161824)
val TvSurfaceVariant = Color(0xFF222538)
val TvPrimary = Color(0xFFE50914)
val TvOnPrimary = Color(0xFFFFFFFF)
val TvFocusBorder = Color(0xFFFFFFFF)
val TvTextPrimary = Color(0xFFEEEEEE)
val TvTextSecondary = Color(0xFFAAAAAA)

private val TvDarkColorScheme = darkColorScheme(
    primary = TvPrimary,
    onPrimary = TvOnPrimary,
    background = TvBackground,
    surface = TvSurface,
    surfaceVariant = TvSurfaceVariant
)

@Composable
fun StreamTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        content = content
    )
}
