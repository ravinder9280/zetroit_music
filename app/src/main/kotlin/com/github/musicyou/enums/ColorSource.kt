package com.github.musicyou.enums

import androidx.annotation.StringRes
import com.github.musicyou.R
import com.github.musicyou.utils.isAtLeastAndroid12

enum class ColorSource(
    @StringRes val resourceId: Int
) {
    Wallpaper(resourceId = R.string.wallpaper_colors),
    // A single accent color; named Preset to keep existing saved settings
    Preset(resourceId = R.string.accent_color),
    Theme(resourceId = R.string.color_theme),
    Custom(resourceId = R.string.custom_colors);

    companion object {
        val Default = Theme

        /** Wallpaper colors only exist on Android 12 and newer; older versions use the accent color. */
        fun effective(source: ColorSource) =
            if (source == Wallpaper && !isAtLeastAndroid12) Preset else source
    }
}
