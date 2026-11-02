package com.zetroit.music.enums

import androidx.annotation.StringRes
import com.zetroit.music.R

enum class ThemeMode(
    @StringRes val resourceId: Int
) {
    System(resourceId = R.string.follow_system),
    Light(resourceId = R.string.light),
    Dark(resourceId = R.string.dark)
}
