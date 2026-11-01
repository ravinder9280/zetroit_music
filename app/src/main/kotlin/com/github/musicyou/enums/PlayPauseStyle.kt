package com.github.musicyou.enums

import androidx.annotation.StringRes
import com.github.musicyou.R

enum class PlayPauseStyle(
    @StringRes val resourceId: Int
) {
    Filled(resourceId = R.string.style_filled),
    Tonal(resourceId = R.string.style_tonal),
    Outlined(resourceId = R.string.style_outlined),
    IconOnly(resourceId = R.string.style_icon_only)
}
