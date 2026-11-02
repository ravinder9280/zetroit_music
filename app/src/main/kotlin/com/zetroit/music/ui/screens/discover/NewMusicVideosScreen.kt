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
import androidx.compose.material3.CircularProgressIndicator
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
import com.github.innertube.requests.newMusicVideos
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.LocalPlayerServiceBinder
import com.zetroit.music.R
import com.zetroit.music.models.LocalMenuState
import com.zetroit.music.ui.components.NonQueuedMediaItemMenu
import com.zetroit.music.ui.items.MusicVideoCard
import com.zetroit.music.utils.asMediaItem
import com.zetroit.music.utils.forcePlay
import com.zetroit.music.viewmodels.HomeDiscoverViewModel

/** Every new music video, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMusicVideosScreen(
    pop: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var videos: Result<List<Innertube.SuggestedResult>>? by remember { mutableStateOf(null) }
    var attempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(attempt) {
        videos = null
        videos = Innertube.newMusicVideos(country = HomeDiscoverViewModel.COUNTRY, all = true)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.new_music_videos)) },
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
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = paddingValues.calculateTopPadding(),
                bottom = 16.dp + playerPadding
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            val loaded = videos?.getOrNull()

            when {
                videos == null -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }

                loaded.isNullOrEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
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
                }

                else -> items(items = loaded, key = { it.item.key }) { result ->
                    val video = result.item as Innertube.VideoItem
                    MusicVideoCard(
                        video = video,
                        subtitle = result.subtitle,
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
    }
}
