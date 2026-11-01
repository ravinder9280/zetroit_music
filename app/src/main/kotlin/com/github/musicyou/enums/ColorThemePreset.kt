package com.github.musicyou.enums

import androidx.compose.ui.graphics.Color
import com.github.musicyou.ui.styling.ThemeColors

/**
 * Spotify's player colors, followed by popular color schemes also offered by Spicetify for
 * Spotify. The values are the official palettes of each theme and its light variant.
 */
enum class ColorThemePreset(
    val displayName: String,
    val dark: ThemeColors,
    val light: ThemeColors
) {
    // The default theme. Spotify only has a dark look, so its light version is worked out
    Spotify(
        displayName = "Spotify",
        dark = spotifyDark,
        light = spotifyDark.adaptedTo(dark = false)
    ),

    // Mocha and Latte
    Catppuccin(
        displayName = "Catppuccin",
        dark = ThemeColors(
            background = Color(0xFF1E1E2E),
            surface = Color(0xFF313244),
            primary = Color(0xFFCBA6F7),
            secondary = Color(0xFFF5C2E7),
            text = Color(0xFFCDD6F4)
        ),
        light = ThemeColors(
            background = Color(0xFFEFF1F5),
            surface = Color(0xFFCCD0DA),
            primary = Color(0xFF8839EF),
            secondary = Color(0xFFEA76CB),
            text = Color(0xFF4C4F69)
        )
    ),

    // Dracula and Alucard
    Dracula(
        displayName = "Dracula",
        dark = ThemeColors(
            background = Color(0xFF282A36),
            surface = Color(0xFF44475A),
            primary = Color(0xFFBD93F9),
            secondary = Color(0xFFFF79C6),
            text = Color(0xFFF8F8F2)
        ),
        light = ThemeColors(
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFCFCFDE),
            primary = Color(0xFF644AC9),
            secondary = Color(0xFFA3144D),
            text = Color(0xFF1F1F1F)
        )
    ),

    // Polar Night and Snow Storm, with Frost accents
    Nord(
        displayName = "Nord",
        dark = ThemeColors(
            background = Color(0xFF2E3440),
            surface = Color(0xFF3B4252),
            primary = Color(0xFF88C0D0),
            secondary = Color(0xFF81A1C1),
            text = Color(0xFFECEFF4)
        ),
        light = ThemeColors(
            background = Color(0xFFECEFF4),
            surface = Color(0xFFD8DEE9),
            primary = Color(0xFF5E81AC),
            secondary = Color(0xFF81A1C1),
            text = Color(0xFF2E3440)
        )
    ),

    // Night and Day
    TokyoNight(
        displayName = "Tokyo Night",
        dark = ThemeColors(
            background = Color(0xFF1A1B26),
            surface = Color(0xFF292E42),
            primary = Color(0xFF7AA2F7),
            secondary = Color(0xFFBB9AF7),
            text = Color(0xFFC0CAF5)
        ),
        light = ThemeColors(
            background = Color(0xFFE1E2E7),
            surface = Color(0xFFC4C8DA),
            primary = Color(0xFF2E7DE9),
            secondary = Color(0xFF9854F1),
            text = Color(0xFF3760BF)
        )
    ),

    // Main and Dawn
    RosePine(
        displayName = "Rosé Pine",
        dark = ThemeColors(
            background = Color(0xFF191724),
            surface = Color(0xFF26233A),
            primary = Color(0xFFEBBCBA),
            secondary = Color(0xFFF6C177),
            text = Color(0xFFE0DEF4)
        ),
        light = ThemeColors(
            background = Color(0xFFFAF4ED),
            surface = Color(0xFFF2E9E1),
            primary = Color(0xFFD7827E),
            secondary = Color(0xFFEA9D34),
            text = Color(0xFF464261)
        )
    );

    fun colors(dark: Boolean) = if (dark) this.dark else light

    companion object {
        val Default = Spotify
    }
}

private val spotifyDark = ThemeColors(
    background = Color(0xFF121212),
    surface = Color(0xFF212121),
    primary = Color(0xFF1DB954),
    secondary = Color(0xFFB3B3B3),
    text = Color.White,
    outline = Color(0xFF535353)
)
