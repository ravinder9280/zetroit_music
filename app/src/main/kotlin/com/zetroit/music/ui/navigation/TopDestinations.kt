package com.zetroit.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import com.zetroit.music.R

object TopDestinations {
    val list = listOf(
        TopDestination(
            route = Routes.Home,
            key = "home",
            resourceId = R.string.home,
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        TopDestination(
            route = Routes.Search,
            key = "search",
            resourceId = R.string.search,
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search,
            isRemembered = false
        ),
        TopDestination(
            route = Routes.Songs,
            key = "songs",
            resourceId = R.string.songs,
            unselectedIcon = Icons.Outlined.MusicNote,
            selectedIcon = Icons.Filled.MusicNote
        ),
        TopDestination(
            route = Routes.Library,
            key = "library",
            resourceId = R.string.library,
            unselectedIcon = Icons.Outlined.LibraryMusic,
            selectedIcon = Icons.Filled.LibraryMusic
        )
    )

    /** Screens opened from a top destination that keep its tab selected. */
    val childRoutes: Map<Any, List<Any>> = mapOf(
        Routes.Library to listOf(Routes.Artists, Routes.Albums)
    )

}