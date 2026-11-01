package com.github.musicyou.enums

import androidx.annotation.StringRes
import com.github.musicyou.R

enum class MiniPlayerButtonStyle(
    @StringRes val resourceId: Int
) {
    Plain(resourceId = R.string.mini_button_plain),
    FilledCircle(resourceId = R.string.mini_button_filled_circle)
}
