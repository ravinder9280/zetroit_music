package com.zetroit.music.enums

import androidx.annotation.StringRes
import com.zetroit.music.R

enum class QuickPicksSource(
    @StringRes val resourceId: Int
) {
    Trending(
        resourceId = R.string.most_played,
    ),
    LastPlayed(
        resourceId = R.string.last_played,
    ),
    Random(
        resourceId = R.string.random,
    )
}