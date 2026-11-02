package com.zetroit.music.enums

import androidx.annotation.StringRes
import com.zetroit.music.R

enum class PlayPauseStyle(
    @StringRes val resourceId: Int
) {
    Filled(resourceId = R.string.style_filled),
    Tonal(resourceId = R.string.style_tonal),
    Outlined(resourceId = R.string.style_outlined),
    IconOnly(resourceId = R.string.style_icon_only)
}
