package com.github.musicyou.enums

import androidx.annotation.StringRes
import com.github.musicyou.R

enum class CornerRoundness(
    @StringRes val resourceId: Int,
    val scale: Float
) {
    Square(resourceId = R.string.corners_square, scale = 0f),
    SlightlyRounded(resourceId = R.string.corners_slightly_rounded, scale = 0.5f),
    Rounded(resourceId = R.string.corners_rounded, scale = 1f),
    ExtraRounded(resourceId = R.string.corners_extra_rounded, scale = 1.5f)
}
