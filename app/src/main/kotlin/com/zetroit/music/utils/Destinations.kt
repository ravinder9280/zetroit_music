package com.zetroit.music.utils

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.zetroit.music.ui.navigation.TopDestinations

fun NavDestination.isTopDestination(): Boolean {
    return TopDestinations.list.any { this.hasRoute(route = it.route::class) }
}

fun Collection<NavDestination>.areTopDestinations(): Boolean {
    return this.all { it.isTopDestination() }
}