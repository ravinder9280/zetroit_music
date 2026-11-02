package com.zetroit.music.ui.screens.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import com.zetroit.music.LocalPlayerServiceBinder
import com.zetroit.music.database
import com.zetroit.music.enums.PlayPauseShape
import com.zetroit.music.enums.PlayPauseStyle
import com.zetroit.music.models.Song
import com.zetroit.music.ui.components.SeekBar
import com.zetroit.music.ui.styling.Dimensions
import com.zetroit.music.ui.styling.LocalPlayerAppearance
import com.zetroit.music.utils.forceSeekToNext
import com.zetroit.music.utils.forceSeekToPrevious
import com.zetroit.music.utils.formatAsDuration
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.utils.trackLoopEnabledKey
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Controls(
    mediaId: String,
    title: String,
    artist: String,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    onGoToArtist: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)
    var scrubbingPosition by remember(mediaId) { mutableStateOf<Long?>(null) }
    var likedAt by rememberSaveable { mutableStateOf<Long?>(null) }
    val playerAppearance = LocalPlayerAppearance.current
    val playPauseSize = playerAppearance.playPauseSize.size

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) playPauseSize / 4 else playPauseSize / 2 }
    )
    val playPauseShape = when (playerAppearance.playPauseShape) {
        PlayPauseShape.Morphing -> RoundedCornerShape(playPauseRoundness)
        PlayPauseShape.Circle -> CircleShape
        PlayPauseShape.RoundedSquare -> RoundedCornerShape(playPauseSize / 4)
        PlayPauseShape.Square -> RectangleShape
    }

    LaunchedEffect(mediaId) {
        database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = title,
            modifier = Modifier.basicMarquee(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = artist,
            modifier = Modifier.clickable(
                enabled = onGoToArtist != null,
                onClick = onGoToArtist ?: {}
            ),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.weight(0.5f)
        )

        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            barHeight = playerAppearance.progressBarThickness.height,
            // A large thumb, at least as big as the thickest bar
            scrubberRadius = maxOf(9.dp, playerAppearance.progressBarThickness.height),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (playerAppearance.showTimeLabels) Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                Text(
                    text = formatAsDuration(duration),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem
                    database.query {
                        if (database.like(
                                mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaId }
                                ?.let {
                                    database.insert(currentMediaItem, Song::toggleLike)
                                }
                        }
                    }
                },
                modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = if (likedAt == null) Icons.Outlined.FavoriteBorder else Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            IconButton(
                onClick = binder.player::forceSeekToPrevious,
                modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            val onPlayPauseClick = {
                if (shouldBePlaying) {
                    binder.player.pause()
                } else {
                    if (binder.player.playbackState == Player.STATE_IDLE) {
                        binder.player.prepare()
                    } else if (binder.player.playbackState == Player.STATE_ENDED) {
                        binder.player.seekToDefaultPosition(0)
                    }
                    binder.player.play()
                }
            }
            val playPauseModifier = Modifier.size(playPauseSize)
            val playPauseIcon = @Composable {
                Icon(
                    imageVector =
                    if (shouldBePlaying) Icons.Filled.Pause
                    else if (binder.player.playbackState == Player.STATE_ENDED) Icons.Filled.Replay
                    else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp * (playPauseSize / 64.dp))
                )
            }

            when (playerAppearance.playPauseStyle) {
                PlayPauseStyle.Filled -> FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = playPauseModifier,
                    shape = playPauseShape,
                    content = playPauseIcon
                )

                PlayPauseStyle.Tonal -> FilledTonalIconButton(
                    onClick = onPlayPauseClick,
                    modifier = playPauseModifier,
                    shape = playPauseShape,
                    content = playPauseIcon
                )

                PlayPauseStyle.Outlined -> OutlinedIconButton(
                    onClick = onPlayPauseClick,
                    modifier = playPauseModifier,
                    shape = playPauseShape,
                    content = playPauseIcon
                )

                PlayPauseStyle.IconOnly -> IconButton(
                    onClick = onPlayPauseClick,
                    modifier = playPauseModifier,
                    shape = playPauseShape,
                    content = playPauseIcon
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IconButton(
                onClick = binder.player::forceSeekToNext,
                modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = { trackLoopEnabled = !trackLoopEnabled },
                modifier = Modifier.weight(1F)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RepeatOne,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(if (trackLoopEnabled) 1F else Dimensions.lowOpacity)
                        .size(28.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
}