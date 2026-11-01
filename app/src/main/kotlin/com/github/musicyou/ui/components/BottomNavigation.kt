package com.github.musicyou.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.musicyou.enums.NavigationLabelsVisibility
import com.github.musicyou.ui.navigation.TopDestinations
import com.github.musicyou.utils.homeScreenTabKey
import com.github.musicyou.utils.navigationLabelsVisibilityKey
import com.github.musicyou.utils.rememberPreference

@Composable
fun BottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var navigationLabelsVisibility by rememberPreference(
        navigationLabelsVisibilityKey,
        NavigationLabelsVisibility.Visible
    )
    val (_, onScreenChanged) = rememberPreference(
        homeScreenTabKey,
        defaultValue = ""
    )

    NavigationBar(
        modifier = if (navigationLabelsVisibility == NavigationLabelsVisibility.Hidden) {
            Modifier.heightIn(max = 90.dp)
        } else Modifier
    ) {
        TopDestinations.list.forEach { destination ->
            val isOnChildRoute = TopDestinations.childRoutes[destination.route].orEmpty()
                .any { currentDestination?.hasRoute(route = it::class) == true }
            val selected = isOnChildRoute ||
                currentDestination?.hierarchy?.any { it.hasRoute(route = destination.route::class) } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected || isOnChildRoute) {
                        if (destination.isRemembered) onScreenChanged(destination.key)
                        navController.navigate(route = destination.route) {
                            popUpTo(id = navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = stringResource(id = destination.resourceId)
                    )
                },
                label = {
                    if (navigationLabelsVisibility != NavigationLabelsVisibility.Hidden) {
                        Text(
                            text = stringResource(id = destination.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                alwaysShowLabel = navigationLabelsVisibility != NavigationLabelsVisibility.VisibleWhenActive
            )
        }
    }
}