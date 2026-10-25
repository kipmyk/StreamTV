package com.streamtv.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.streamtv.app.ui.theme.TvSurface
import com.streamtv.app.ui.theme.TvSurfaceVariant
import com.streamtv.app.ui.theme.TvTextPrimary
import com.streamtv.app.ui.theme.TvTextSecondary

enum class NavDestination(val title: String, val icon: String) {
    HOME("Home", "🏠"),
    MOVIES("Movies", "🎬"),
    SERIES("Series", "📺"),
    LIVE("Live TV", "📡"),
    SPORTS("Sports", "⚽"),
    SETTINGS("Settings", "⚙️")
}

@Composable
fun TvSideBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(TvSurface)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // App Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(
                text = "STREAM",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TV",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TvPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        NavDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination
            var isFocused by remember { mutableStateOf(false) }

            val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f, label = "scale")
            val bgColor by animateColorAsState(
                when {
                    isFocused -> TvPrimary
                    isSelected -> TvSurfaceVariant
                    else -> Color.Transparent
                },
                label = "bgColor"
            )

            Surface(
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused }
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = if (isFocused) TvFocusBorder else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = bgColor,
                    focusedContainerColor = TvPrimary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = destination.icon,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = destination.title,
                        color = if (isFocused || isSelected) TvTextPrimary else TvTextSecondary,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
