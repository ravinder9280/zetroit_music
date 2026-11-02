package com.zetroit.music.ui.screens.home

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.innertube.Innertube
import com.github.innertube.models.NavigationEndpoint
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.LocalPlayerServiceBinder
import com.zetroit.music.R
import com.zetroit.music.database
import com.zetroit.music.enums.QuickPicksSource
import com.zetroit.music.models.LocalMenuState
import com.zetroit.music.ui.components.AppTitle
import com.zetroit.music.ui.components.HomeScaffold
import com.zetroit.music.ui.components.NonQueuedMediaItemMenu
import com.zetroit.music.ui.components.ShimmerHost
import com.zetroit.music.ui.components.TextPlaceholder
import com.zetroit.music.ui.items.AlbumItem
import com.zetroit.music.ui.items.ArtistItem
import com.zetroit.music.ui.items.ItemPlaceholder
import com.zetroit.music.ui.items.ListItemPlaceholder
import com.zetroit.music.ui.items.LocalSongItem
import com.zetroit.music.ui.items.PlaylistItem
import com.zetroit.music.ui.items.SongItem
import com.zetroit.music.ui.styling.Dimensions
import com.zetroit.music.utils.asMediaItem
import com.zetroit.music.utils.forcePlay
import com.zetroit.music.utils.isLandscape
import com.zetroit.music.utils.quickPicksSourceKey
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.viewmodels.HomeDiscoverViewModel
import com.zetroit.music.viewmodels.QuickPicksViewModel
import kotlinx.coroutines.launch

@Composable
fun QuickPicks(
    openSearch: () -> Unit,
    openSettings: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMoodClick: (Innertube.Mood) -> Unit,
    onNewReleasesClick: () -> Unit,
    onNewMusicVideosClick: () -> Unit,
    onSongsClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    val viewModel: QuickPicksViewModel = viewModel()
    val discoverViewModel: HomeDiscoverViewModel = viewModel()
    val quickPicksSource by rememberPreference(quickPicksSourceKey, QuickPicksSource.Trending)
    val scope = rememberCoroutineScope()

    val songThumbnailSizeDp = Dimensions.thumbnails.song
    val itemSize = 108.dp + 2 * 8.dp
    val quickPicksLazyGridState = rememberLazyGridState()
    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 12.dp)

    LaunchedEffect(quickPicksSource) {
        viewModel.loadQuickPicks(quickPicksSource = quickPicksSource)
    }

    HomeScaffold(
        title = R.string.app_name,
        titleContent = { AppTitle() },
        openSearch = openSearch,
        openSettings = openSettings
    ) {
        BoxWithConstraints {
            val quickPicksLazyGridItemWidthFactor =
                if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f

            // Lets a fling carry across several columns before settling on one
            val snapLayoutInfoProvider = remember(quickPicksLazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = quickPicksLazyGridState,
                    snapPosition = SnapPosition.Start
                )
            }

            val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

            val relatedPage = viewModel.relatedPageResult?.getOrNull()

            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp + playerPadding),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "quickPicks") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        relatedPage?.let { related ->
                            Text(
                                text = stringResource(id = R.string.quick_picks),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = sectionTextModifier
                            )

                            LazyHorizontalGrid(
                                state = quickPicksLazyGridState,
                                rows = GridCells.Fixed(count = 4),
                                flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((songThumbnailSizeDp + Dimensions.itemsVerticalPadding * 2) * 4)
                            ) {
                                viewModel.trending?.let { song ->
                                    item {
                                        LocalSongItem(
                                            modifier = Modifier
                                                .animateItem()
                                                .width(itemInHorizontalGridWidth),
                                            song = song,
                                            onClick = {
                                                val mediaItem = song.asMediaItem
                                                binder?.stopRadio()
                                                binder?.player?.forcePlay(mediaItem)
                                                binder?.setupRadio(
                                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                                )
                                            },
                                            onLongClick = {
                                                menuState.display {
                                                    NonQueuedMediaItemMenu(
                                                        onDismiss = menuState::hide,
                                                        mediaItem = song.asMediaItem,
                                                        onRemoveFromQuickPicks = {
                                                            database.query {
                                                                database.clearEventsFor(song.id)
                                                            }
                                                        },
                                                        onGoToAlbum = onAlbumClick,
                                                        onGoToArtist = onArtistClick
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }

                                val trendingCount = if (viewModel.trending == null) 0 else 1
                                val relatedSongs = related.songs.orEmpty()
                                val songCount = relatedSongs.size + trendingCount

                                items(
                                    // Keeps every column of the grid filled
                                    items = if (songCount < 4) relatedSongs else relatedSongs.take(songCount / 4 * 4 - trendingCount),
                                    key = Innertube.SongItem::key
                                ) { song ->
                                    SongItem(
                                        modifier = Modifier
                                            .animateItem()
                                            .width(itemInHorizontalGridWidth),
                                        song = song,
                                        onClick = {
                                            val mediaItem = song.asMediaItem
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(mediaItem)
                                            binder?.setupRadio(
                                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                            )
                                        },
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem,
                                                    onGoToAlbum = onAlbumClick,
                                                    onGoToArtist = onArtistClick
                                                )
                                            }
                                        }
                                    )
                                }
                            }

                            Unit
                        } ?: viewModel.relatedPageResult?.exceptionOrNull()?.let {
                            Text(
                                text = stringResource(id = R.string.home_error),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(all = 16.dp)
                            )

                            Row(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.loadQuickPicks(quickPicksSource)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = stringResource(id = R.string.retry)
                                    )

                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                                    Text(text = stringResource(id = R.string.retry))
                                }

                                FilledTonalButton(
                                    onClick = onDownloadsClick
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DownloadDone,
                                        contentDescription = stringResource(id = R.string.downloads)
                                    )

                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                                    Text(text = stringResource(id = R.string.downloads))
                                }
                            }
                        } ?: ShimmerHost {
                            TextPlaceholder(modifier = sectionTextModifier)

                            repeat(4) {
                                ListItemPlaceholder()
                            }
                        }
                    }
                }

                item(key = "recentlyPlayed") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Discovery sections; each loads and fails on its own, and the listening
                        // history section also works offline. It adds its own spacing, as it is
                        // only shown once something has been played.
                        RecentlyPlayedSection(
                            itemSize = itemSize,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onViewAll = onSongsClick
                        )
                    }
                }

                item(key = "madeForYou") {
                    // Fetched only once this section scrolls into view
                    LaunchedEffect(Unit) { discoverViewModel.requestMadeForYou() }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                        MadeForYouSection(
                            result = discoverViewModel.madeForYou,
                            itemSize = itemSize,
                            songItemWidth = itemInHorizontalGridWidth,
                            onRetry = { discoverViewModel.requestMadeForYou() },
                            onPlaylistClick = onPlaylistClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick
                        )
                    }
                }

                item(key = "trending") {
                    // Fetched only once this section scrolls into view
                    LaunchedEffect(Unit) { discoverViewModel.requestTrending() }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                        TrendingSection(
                            charts = discoverViewModel.charts,
                            trendingSongs = discoverViewModel.trendingSongs,
                            itemSize = itemSize,
                            songItemWidth = itemInHorizontalGridWidth,
                            onRetry = { discoverViewModel.requestTrending() },
                            onPlaylistClick = onPlaylistClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick
                        )
                    }
                }

                item(key = "newReleases") {
                    // Fetched only once this section scrolls into view
                    LaunchedEffect(Unit) { discoverViewModel.requestNewReleases() }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                        NewReleasesSection(
                            result = discoverViewModel.newReleases,
                            itemSize = itemSize,
                            onRetry = { discoverViewModel.requestNewReleases() },
                            onAlbumClick = onAlbumClick,
                            onViewAll = onNewReleasesClick
                        )
                    }
                }

                item(key = "newMusicVideos") {
                    // Fetched only once this section scrolls into view
                    LaunchedEffect(Unit) { discoverViewModel.requestNewMusicVideos() }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                        NewMusicVideosSection(
                            result = discoverViewModel.newMusicVideos,
                            onRetry = { discoverViewModel.requestNewMusicVideos() },
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onViewAll = onNewMusicVideosClick
                        )
                    }
                }

                item(key = "moods") {
                    // Fetched only once this section scrolls into view
                    LaunchedEffect(Unit) { discoverViewModel.requestMoods() }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                        MoodsSection(
                            result = discoverViewModel.moodsAndGenres,
                            onRetry = { discoverViewModel.requestMoods() },
                            onMoodClick = onMoodClick
                        )
                    }
                }

                item(key = "related") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        relatedPage?.let { related ->
                            related.albums?.let { albums ->
                                Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                                Text(
                                    text = stringResource(id = R.string.related_albums),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = sectionTextModifier
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(
                                        items = albums,
                                        key = Innertube.AlbumItem::key
                                    ) { album ->
                                        AlbumItem(
                                            modifier = Modifier.widthIn(max = itemSize),
                                            album = album,
                                            onClick = { onAlbumClick(album.key) }
                                        )
                                    }
                                }
                            }

                            related.artists?.let { artists ->
                                Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                                Text(
                                    text = stringResource(id = R.string.similar_artists),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = sectionTextModifier
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(
                                        items = artists,
                                        key = Innertube.ArtistItem::key,
                                    ) { artist ->
                                        ArtistItem(
                                            modifier = Modifier.widthIn(max = itemSize),
                                            artist = artist,
                                            onClick = { onArtistClick(artist.key) }
                                        )
                                    }
                                }
                            }

                            related.playlists?.let { playlists ->
                                Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                                Text(
                                    text = stringResource(id = R.string.recommended_playlists),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = sectionTextModifier
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(
                                        items = playlists,
                                        key = Innertube.PlaylistItem::key,
                                    ) { playlist ->
                                        PlaylistItem(
                                            modifier = Modifier.widthIn(max = itemSize),
                                            playlist = playlist,
                                            onClick = { onPlaylistClick(playlist.key) }
                                        )
                                    }
                                }
                            }

                            Unit
                        } ?: if (viewModel.relatedPageResult == null) ShimmerHost {
                            TextPlaceholder(modifier = sectionTextModifier)

                            Row(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                repeat(2) {
                                    ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                            TextPlaceholder(modifier = sectionTextModifier)

                            Row(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                repeat(2) {
                                    ItemPlaceholder(
                                        modifier = Modifier.widthIn(max = itemSize),
                                        shape = CircleShape
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

                            TextPlaceholder(modifier = sectionTextModifier)

                            Row(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                repeat(2) {
                                    ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                                }
                            }
                        } else Unit
                    }
                }
            }
        }
    }
}
