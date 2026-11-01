package com.github.musicyou.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TopDestination<T : Any>(
    val route: T,
    val key: String,
    @StringRes val resourceId: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    // Whether the app opens on this tab again after being closed on it
    val isRemembered: Boolean = true
)