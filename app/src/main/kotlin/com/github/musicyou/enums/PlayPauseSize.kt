package com.github.musicyou.enums

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.musicyou.R

enum class PlayPauseSize(
    @StringRes val resourceId: Int,
    val size: Dp
) {
    Small(resourceId = R.string.text_size_small, size = 56.dp),
    Default(resourceId = R.string.text_size_default, size = 64.dp),
    Large(resourceId = R.string.text_size_large, size = 80.dp)
}
