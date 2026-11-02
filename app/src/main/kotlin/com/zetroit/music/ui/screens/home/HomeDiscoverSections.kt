package com.zetroit.music.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.innertube.Innertube
import com.zetroit.music.LocalPlayerServiceBinder
import com.zetroit.music.R
import com.zetroit.music.database
import com.zetroit.music.models.LocalMenuState
import com.zetroit.music.models.RecentSong
import com.zetroit.music.models.Song
import com.zetroit.music.ui.components.InHistoryMediaItemMenu
import com.zetroit.music.ui.components.NonQueuedMediaItemMenu
import com.zetroit.music.ui.components.ShimmerHost
import com.zetroit.music.ui.components.TextPlaceholder
import com.zetroit.music.ui.items.AlbumItem
import com.zetroit.music.ui.items.ItemContainer
import com.zetroit.music.ui.items.ItemPlaceholder
import com.zetroit.music.ui.items.ListItemPlaceholder
import com.zetroit.music.ui.items.MusicVideoCard
import com.zetroit.music.ui.items.PlaylistItem
import com.zetroit.music.ui.items.SongItem
import com.zetroit.music.ui.styling.Dimensions
import com.zetroit.music.ui.styling.px
import com.zetroit.music.utils.asMediaItem
import com.zetroit.music.utils.forcePlay
import com.zetroit.music.utils.forcePlayAtIndex
import com.zetroit.music.utils.thumbnail
import com.zetroit.music.viewmodels.MadeForYou
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

private val sectionTextModifier = Modifier
    .padding(horizontal = 16.dp)
    .padding(bottom = 8.dp)

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, bottom = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(Dimensions.mediumOpacity)
                )
            }
        }

        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
private fun SectionMessage(
    text: String,
    onRetry: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .alpha(Dimensions.mediumOpacity)
        )

        onRetry?.let {
            TextButton(onClick = it) {
                Text(text = stringResource(id = R.string.retry))
            }
        }
    }
}

@Composable
private fun CardsPlaceholder(itemSize: Dp, songRows: Int = 0) {
    ShimmerHost {
        TextPlaceholder(modifier = sectionTextModifier)

        repeat(songRows) {
            ListItemPlaceholder()
        }

        if (songRows == 0) {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                repeat(2) {
                    ItemPlaceholder(modifier = Modifier.widthIn(max = itemSize))
                }
            }
        }
    }
}

/** The same four-row, snapping grid as Quick picks. */
@Composable
private fun SongGrid(
    songs: List<Innertube.SongItem>,
    itemWidth: Dp,
    showRank: Boolean = false,
    onClick: (index: Int) -> Unit,
    onLongClick: (Innertube.SongItem) -> Unit
) {
    val rows = songs.size.coerceIn(1, 4)
    val gridState = rememberLazyGridState()
    val snapLayoutInfoProvider = remember(gridState) {
        SnapLayoutInfoProvider(lazyGridState = gridState, snapPosition = SnapPosition.Start)
    }

    LazyHorizontalGrid(
        state = gridState,
        rows = GridCells.Fixed(count = rows),
        flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
        modifier = Modifier
            .fillMaxWidth()
            .height((Dimensions.thumbnails.song + Dimensions.itemsVerticalPadding * 2) * rows)
    ) {
        itemsIndexed(
            items = songs,
            key = { _, song -> song.key }
        ) { index, song ->
            SongItem(
                modifier = Modifier.width(itemWidth),
                song = song,
                onClick = { onClick(index) },
                onLongClick = { onLongClick(song) },
                trailingContent = if (showRank) ({
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }) else null
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlists: List<Innertube.PlaylistItem>,
    itemSize: Dp,
    onPlaylistClick: (String) -> Unit
) {
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(items = playlists, key = Innertube.PlaylistItem::key) { playlist ->
            PlaylistItem(
                modifier = Modifier.widthIn(max = itemSize),
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.key) }
            )
        }
    }
}

/**
 * The songs most recently played on this device, newest first. Tapping one plays it, with
 * the other recent songs queued after it.
 */
@Composable
fun RecentlyPlayedSection(
    itemSize: Dp,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val songs by remember { database.recentlyPlayedSongs() }
        .collectAsState(initial = null, context = Dispatchers.IO)
    // Hidden entirely until there is something to show
    val recentSongs = songs?.map(RecentSong::song)?.takeIf { it.isNotEmpty() } ?: return

    Spacer(modifier = Modifier.height(Dimensions.homeSectionSpacer))

    SectionHeader(
        title = stringResource(id = R.string.recently_played),
        actionText = stringResource(id = R.string.view_all),
        onAction = onViewAll
    )

    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        itemsIndexed(items = recentSongs, key = { _, song -> song.id }) { index, song ->
            ItemContainer(
                modifier = Modifier.widthIn(max = itemSize),
                title = song.title,
                subtitle = song.artistsText,
                onClick = {
                    binder?.stopRadio()
                    binder?.player?.forcePlayAtIndex(recentSongs.map(Song::asMediaItem), index)
                },
                onLongClick = {
                    menuState.display {
                        InHistoryMediaItemMenu(
                            song = song,
                            onDismiss = menuState::hide,
                            onGoToAlbum = onAlbumClick,
                            onGoToArtist = onArtistClick
                        )
                    }
                }
            ) {
                if (song.thumbnailUrl == null) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = song.thumbnailUrl.thumbnail(maxWidth.px),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(maxWidth)
                                .clip(MaterialTheme.shapes.large)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MadeForYouSection(
    result: Result<MadeForYou>?,
    itemSize: Dp,
    songItemWidth: Dp,
    onRetry: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    if (result == null) {
        CardsPlaceholder(itemSize = itemSize, songRows = 4)
        return
    }

    val madeForYou = result.getOrNull()

    SectionHeader(
        title = stringResource(id = R.string.made_for_you),
        subtitle = when {
            madeForYou == null -> null
            !madeForYou.isPersonalized -> stringResource(id = R.string.made_for_you_new_user)
            madeForYou.basedOnArtists.isNotEmpty() ->
                stringResource(id = R.string.based_on, madeForYou.basedOnArtists.joinToString())

            else -> stringResource(id = R.string.based_on_your_listening)
        }
    )

    if (madeForYou == null) {
        SectionMessage(text = stringResource(id = R.string.home_error), onRetry = onRetry)
        return
    }

    if (madeForYou.songs.isNotEmpty()) {
        SongGrid(
            songs = madeForYou.songs,
            itemWidth = songItemWidth,
            // Plays the picks in order, like a personal mix
            onClick = { index ->
                binder?.stopRadio()
                binder?.player?.forcePlayAtIndex(madeForYou.songs.map(Innertube.SongItem::asMediaItem), index)
            },
            onLongClick = { song ->
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

    if (madeForYou.playlists.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        PlaylistRow(playlists = madeForYou.playlists, itemSize = itemSize, onPlaylistClick = onPlaylistClick)
    }
}

@Composable
fun TrendingSection(
    charts: Result<Innertube.ChartsPage>?,
    trendingSongs: Result<List<Innertube.SongItem>>?,
    itemSize: Dp,
    songItemWidth: Dp,
    onRetry: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    if (trendingSongs == null) {
        CardsPlaceholder(itemSize = itemSize, songRows = 4)
        return
    }

    val songs = trendingSongs.getOrNull()
    val trendingChart = charts?.getOrNull()?.charts?.firstOrNull()

    SectionHeader(
        title = stringResource(id = R.string.trending_in_india),
        subtitle = stringResource(id = R.string.trending_source),
        actionText = stringResource(id = R.string.view_all).takeIf { trendingChart != null },
        onAction = trendingChart?.let { { onPlaylistClick(it.key) } }
    )

    if (songs.isNullOrEmpty()) {
        SectionMessage(
            text = stringResource(id = if (songs == null) R.string.home_error else R.string.no_items_found),
            onRetry = onRetry
        )
        return
    }

    SongGrid(
        songs = songs,
        itemWidth = songItemWidth,
        showRank = true,
        onClick = { index ->
            binder?.stopRadio()
            binder?.player?.forcePlayAtIndex(songs.map(Innertube.SongItem::asMediaItem), index)
        },
        onLongClick = { song ->
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

@Composable
fun NewReleasesSection(
    result: Result<List<Innertube.AlbumItem>>?,
    itemSize: Dp,
    onRetry: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    if (result == null) {
        CardsPlaceholder(itemSize = itemSize)
        return
    }

    val albums = result.getOrNull()

    SectionHeader(
        title = stringResource(id = R.string.new_releases),
        actionText = stringResource(id = R.string.view_all).takeIf { !albums.isNullOrEmpty() },
        onAction = onViewAll
    )

    if (albums.isNullOrEmpty()) {
        SectionMessage(
            text = stringResource(id = if (albums == null) R.string.home_error else R.string.no_items_found),
            onRetry = onRetry
        )
        return
    }

    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(items = albums, key = Innertube.AlbumItem::key) { album ->
            AlbumItem(
                modifier = Modifier.widthIn(max = itemSize),
                album = album,
                onClick = { onAlbumClick(album.key) }
            )
        }
    }
}

@Composable
fun NewMusicVideosSection(
    result: Result<List<Innertube.SuggestedResult>>?,
    onRetry: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    if (result == null) {
        ShimmerHost {
            TextPlaceholder(modifier = sectionTextModifier)
            Row(modifier = Modifier.padding(start = 8.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(width = 260.dp, height = 146.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
        return
    }

    val videos = result.getOrNull()

    SectionHeader(
        title = stringResource(id = R.string.new_music_videos),
        actionText = stringResource(id = R.string.view_all).takeIf { !videos.isNullOrEmpty() },
        onAction = onViewAll
    )

    if (videos.isNullOrEmpty()) {
        SectionMessage(
            text = stringResource(id = if (videos == null) R.string.home_error else R.string.no_items_found),
            onRetry = onRetry
        )
        return
    }

    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(items = videos, key = { it.item.key }) { result ->
            val video = result.item as Innertube.VideoItem
            MusicVideoCard(
                video = video,
                subtitle = result.subtitle,
                modifier = Modifier.width(260.dp),
                onClick = {
                    binder?.stopRadio()
                    binder?.player?.forcePlay(video.asMediaItem)
                    binder?.setupRadio(video.info?.endpoint)
                },
                onLongClick = {
                    menuState.display {
                        NonQueuedMediaItemMenu(
                            onDismiss = menuState::hide,
                            mediaItem = video.asMediaItem,
                            onGoToAlbum = onAlbumClick,
                            onGoToArtist = onArtistClick
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun MoodsSection(
    result: Result<Innertube.MoodsAndGenresPage>?,
    onRetry: () -> Unit,
    onMoodClick: (Innertube.Mood) -> Unit
) {
    if (result == null) {
        ShimmerHost {
            TextPlaceholder(modifier = sectionTextModifier)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 56.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
        return
    }

    val moods = result.getOrNull()?.moods

    SectionHeader(title = stringResource(id = R.string.browse_by_mood))

    if (moods.isNullOrEmpty()) {
        SectionMessage(
            text = stringResource(id = if (moods == null) R.string.home_error else R.string.no_items_found),
            onRetry = onRetry
        )
        return
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(count = 2),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp * 2 + 8.dp)
    ) {
        itemsIndexed(items = moods, key = { _, mood -> mood.title }) { _, mood ->
            MoodItem(mood = mood, onClick = { onMoodClick(mood) })
        }
    }
}

@Composable
fun MoodItem(
    mood: Innertube.Mood,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .size(width = 150.dp, height = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
    ) {
        // YouTube Music gives every mood its own color
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(mood.stripeColor?.let { Color(it.toInt()) } ?: MaterialTheme.colorScheme.primary)
        )

        Text(
            text = mood.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
