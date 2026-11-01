package com.github.musicyou.enums

import androidx.annotation.StringRes
import com.github.musicyou.R

enum class TextSize(
    @StringRes val resourceId: Int,
    val scale: Float
) {
    Small(resourceId = R.string.text_size_small, scale = 0.9f),
    Default(resourceId = R.string.text_size_default, scale = 1f),
    Large(resourceId = R.string.text_size_large, scale = 1.1f),
    ExtraLarge(resourceId = R.string.text_size_extra_large, scale = 1.2f)
}
