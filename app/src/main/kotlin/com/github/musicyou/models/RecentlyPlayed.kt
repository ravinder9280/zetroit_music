package com.github.musicyou.models

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class RecentSong(
    @Embedded val song: Song,
    val lastPlayedAt: Long
)
