package com.github.musicyou.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.github.musicyou.R

enum class AccentColor(
    @StringRes val resourceId: Int,
    val seed: Color
) {
    // Uses the app's original hand-tuned color scheme
    Indigo(resourceId = R.string.accent_indigo, seed = Color(0xFF445E91)),
    Blue(resourceId = R.string.accent_blue, seed = Color(0xFF1E88E5)),
    Teal(resourceId = R.string.accent_teal, seed = Color(0xFF00897B)),
    Green(resourceId = R.string.accent_green, seed = Color(0xFF43A047)),
    Orange(resourceId = R.string.accent_orange, seed = Color(0xFFFB8C00)),
    Red(resourceId = R.string.accent_red, seed = Color(0xFFE53935)),
    Pink(resourceId = R.string.accent_pink, seed = Color(0xFFD81B60)),
    Purple(resourceId = R.string.accent_purple, seed = Color(0xFF8E24AA))
}
