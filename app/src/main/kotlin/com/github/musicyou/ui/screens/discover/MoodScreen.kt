package com.github.musicyou.ui.screens.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.github.innertube.Innertube
import com.github.innertube.models.NavigationEndpoint
import com.github.innertube.requests.moodPage
import com.github.innertube.requests.playlistPage
import com.github.musicyou.LocalPlayerPadding
import com.github.musicyou.LocalPlayerServiceBinder
import com.github.musicyou.R
import com.github.musicyou.ui.components.ShimmerHost
import com.github.musicyou.ui.components.TextPlaceholder
import com.github.musicyou.ui.items.ItemPlaceholder
import com.github.musicyou.ui.items.PlaylistItem
import com.github.musicyou.utils.asMediaItem
import com.github.musicyou.utils.forcePlayFromBeginning
import com.github.musicyou.utils.toast
import com.github.musicyou.viewmodels.HomeDiscoverViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** The playlists of a mood or genre, with buttons to play songs from its top playlists. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    title: String,
    browseId: String,
    params: String?,
    pop: () -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val playerPadding = LocalPlayerPadding.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val mood = remember(title, browseId, params) {
        Innertube.Mood(
            title = title,
            endpoint = NavigationEndpoint.Endpoint.Browse(browseId = browseId, params = params),
            stripeColor = null
        )
    }

    var sections: Result<List<Innertube.PlaylistsSection>>? by remember { mutableStateOf(null) }
    var attempt by remember { mutableIntStateOf(0) }
    var isPreparingPlayback by remember { mutableStateOf(false) }
    val loadFailed = stringResource(id = R.string.playback_load_failed)

    LaunchedEffect(mood, attempt) {
        sections = null
        sections = Innertube.moodPage(mood = mood, country = HomeDiscoverViewModel.COUNTRY)
    }

    fun playAll(shuffle: Boolean) {
        val topPlaylists = sections?.getOrNull()?.firstOrNull()?.playlists ?: return
        isPreparingPlayback = true

        scope.launch {
            val mediaItems = songsOf(topPlaylists)
            isPreparingPlayback = false

            if (mediaItems.isEmpty()) {
                context.toast(loadFailed)
                return@launch
            }
            binder?.stopRadio()
            binder?.player?.forcePlayFromBeginning(if (shuffle) mediaItems.shuffled() else mediaItems)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = 16.dp + playerPadding
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "actions") {
                val canPlay = sections?.getOrNull()?.isNotEmpty() == true && !isPreparingPlayback

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(onClick = { playAll(shuffle = false) }, enabled = canPlay) {
                        Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(id = R.string.play_all))
                    }

                    FilledTonalButton(onClick = { playAll(shuffle = true) }, enabled = canPlay) {
                        Icon(imageVector = Icons.Outlined.Shuffle, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(id = R.string.shuffle))
                    }

                    if (isPreparingPlayback) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }

            when (val result = sections) {
                null -> item(key = "loading") {
                    ShimmerHost {
                        repeat(3) {
                            TextPlaceholder(modifier = Modifier.padding(16.dp))
                            Row(modifier = Modifier.padding(start = 8.dp)) {
                                repeat(2) { ItemPlaceholder(modifier = Modifier.widthIn(max = 124.dp)) }
                            }
                        }
                    }
                }

                else -> {
                    val loaded = result.getOrNull()

                    if (loaded.isNullOrEmpty()) item(key = "message") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (loaded == null) R.string.home_error else R.string.no_items_found
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (loaded == null) {
                                TextButton(onClick = { attempt++ }) {
                                    Text(text = stringResource(id = R.string.retry))
                                }
                            }
                        }
                    } else items(items = loaded, key = { it.title + it.playlists.first().key }) { section ->
                        section.title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                            items(items = section.playlists, key = Innertube.PlaylistItem::key) { playlist ->
                                PlaylistItem(
                                    modifier = Modifier.widthIn(max = 124.dp),
                                    playlist = playlist,
                                    onClick = { onPlaylistClick(playlist.key) }
                                )
                            }
                        }
                    }
                }
            }

            item(key = "bottom") { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

/** Songs of the first few playlists, loaded in parallel, without repeats. */
private suspend fun songsOf(
    playlists: List<Innertube.PlaylistItem>,
    playlistCount: Int = 3
): List<MediaItem> = coroutineScope {
    playlists.take(playlistCount)
        .map { playlist ->
            async {
                Innertube.playlistPage(browseId = playlist.key)?.getOrNull()?.songsPage?.items.orEmpty()
            }
        }
        .awaitAll()
        .flatten()
        .distinctBy(Innertube.SongItem::key)
        .map(Innertube.SongItem::asMediaItem)
}
