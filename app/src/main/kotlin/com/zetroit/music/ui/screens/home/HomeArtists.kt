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
import com.zetroit.music.enums.ArtistSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.Artist
import com.zetroit.music.ui.components.HomeScaffold
import com.zetroit.music.ui.components.SortingHeader
import com.zetroit.music.ui.items.LocalArtistItem
import com.zetroit.music.utils.artistSortByKey
import com.zetroit.music.utils.artistSortOrderKey
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.viewmodels.HomeArtistsViewModel

@Composable
fun HomeArtistList(
    pop: () -> Unit,
    openSearch: () -> Unit,
    openSettings: () -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    val playerPadding = LocalPlayerPadding.current

    var sortBy by rememberPreference(artistSortByKey, ArtistSortBy.Name)
    var sortOrder by rememberPreference(artistSortOrderKey, SortOrder.Ascending)

    val viewModel: HomeArtistsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadArtists(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    HomeScaffold(
        pop = pop,
        title = R.string.artists,
        openSearch = openSearch,
        openSettings = openSettings
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
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
                span = { GridItemSpan(maxLineSpan) }
            ) {
                SortingHeader(
                    sortBy = sortBy,
                    changeSortBy = { sortBy = it },
                    sortByEntries = ArtistSortBy.entries.toList(),
                    sortOrder = sortOrder,
                    toggleSortOrder = { sortOrder = !sortOrder },
                    size = viewModel.items.size,
                    itemCountText = R.plurals.number_of_artists
                )
            }

            items(items = viewModel.items, key = Artist::id) { artist ->
                LocalArtistItem(
                    modifier = Modifier.animateItem(),
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}