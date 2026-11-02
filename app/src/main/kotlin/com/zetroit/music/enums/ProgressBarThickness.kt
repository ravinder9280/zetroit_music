package com.zetroit.music.enums

import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zetroit.music.R

enum class ProgressBarThickness(
    @StringRes val resourceId: Int,
    val height: Dp
) {
    Thin(resourceId = R.string.thickness_thin, height = 3.dp),
    Medium(resourceId = R.string.thickness_medium, height = 6.dp),
    Thick(resourceId = R.string.thickness_thick, height = 10.dp)
}
