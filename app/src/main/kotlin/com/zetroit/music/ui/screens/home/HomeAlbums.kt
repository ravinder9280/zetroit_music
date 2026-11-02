package com.zetroit.music.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.R
import com.zetroit.music.enums.AlbumSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.Album
import com.zetroit.music.ui.components.HomeScaffold
import com.zetroit.music.ui.components.SortingHeader
import com.zetroit.music.ui.items.LocalAlbumItem
import com.zetroit.music.utils.albumSortByKey
import com.zetroit.music.utils.albumSortOrderKey
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.viewmodels.HomeAlbumsViewModel

@Composable
fun HomeAlbums(
    pop: () -> Unit,
    openSearch: () -> Unit,
    openSettings: () -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.Title)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Ascending)

    val viewModel: HomeAlbumsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadAlbums(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    HomeScaffold(
        pop = pop,
        title = R.string.albums,
        openSearch = openSearch,
        openSettings = openSettings
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                bottom = 16.dp + playerPadding
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                span = { GridItemSpan(maxCurrentLineSpan) }
            ) {
                SortingHeader(
                    sortBy = sortBy,
                    changeSortBy = { sortBy = it },
                    sortByEntries = AlbumSortBy.entries.toList(),
                    sortOrder = sortOrder,
                    toggleSortOrder = { sortOrder = !sortOrder },
                    size = viewModel.items.size,
                    itemCountText = R.plurals.number_of_albums
                )
            }

            items(
                items = viewModel.items,
                key = Album::id
            ) { album ->
                LocalAlbumItem(
                    modifier = Modifier.animateItem(),
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}