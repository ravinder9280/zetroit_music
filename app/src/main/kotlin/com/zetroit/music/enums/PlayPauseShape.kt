package com.zetroit.music.enums

import androidx.annotation.StringRes
import com.zetroit.music.R

enum class PlayPauseShape(
    @StringRes val resourceId: Int
) {
    // Rounded square while playing, circle while paused
    Morphing(resourceId = R.string.shape_morphing),
    Circle(resourceId = R.string.shape_circle),
    RoundedSquare(resourceId = R.string.shape_rounded_square),
    Square(resourceId = R.string.shape_square)
}
