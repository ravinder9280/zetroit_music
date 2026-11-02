package com.zetroit.music.ui.screens.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.innertube.Innertube
import com.github.innertube.requests.newReleases
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.R
import com.zetroit.music.ui.components.ShimmerHost
import com.zetroit.music.ui.items.AlbumItem
import com.zetroit.music.ui.items.ItemPlaceholder
import com.zetroit.music.viewmodels.HomeDiscoverViewModel

/** Every new single, EP and album, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReleasesScreen(
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val playerPadding = LocalPlayerPadding.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var releases: Result<List<Innertube.AlbumItem>>? by remember { mutableStateOf(null) }
    var attempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(attempt) {
        releases = null
        releases = Innertube.newReleases(country = HomeDiscoverViewModel.COUNTRY, all = true)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.new_releases)) },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = paddingValues.calculateTopPadding(),
                bottom = 16.dp + playerPadding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val albums = releases?.getOrNull()

            when {
                releases == null -> items(count = 6) {
                    ShimmerHost { ItemPlaceholder() }
                }

                albums.isNullOrEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = if (albums == null) R.string.home_error else R.string.no_items_found
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (albums == null) {
                            TextButton(onClick = { attempt++ }) {
                                Text(text = stringResource(id = R.string.retry))
                            }
                        }
                    }
                }

                else -> items(items = albums, key = Innertube.AlbumItem::key) { album ->
                    AlbumItem(album = album, onClick = { onAlbumClick(album.key) })
                }
            }
        }
    }
}
