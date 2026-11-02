package com.zetroit.music.ui.styling

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * The few colors that define a whole theme. Everything else in the Material color
 * scheme (containers, borders, text on top of colors) is derived from them.
 */
@Immutable
data class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    // Taken from the palette for presets; worked out from the background for custom themes
    val text: Color = readableTextOn(background),
    // Borders and dividers; worked out from the text and background when not given
    val outline: Color? = null
) {
    val isDark: Boolean
        get() = background.toLch().lightness < 50f

    /**
     * Returns the colors unchanged when they already suit the requested mode, otherwise
     * builds the opposite mode keeping each color's hue, so one custom theme works in both.
     */
    fun adaptedTo(dark: Boolean): ThemeColors {
        if (isDark == dark) return this

        fun Color.withTone(tone: Float, maxChroma: Float = Float.MAX_VALUE): Color {
            val lch = toLch()
            return lchToColor(tone, minOf(lch.chroma, maxChroma), lch.hue)
        }

        val background = background.withTone(if (dark) 8f else 97f, maxChroma = 12f)
        return ThemeColors(
            background = background,
            surface = surface.withTone(if (dark) 18f else 91f, maxChroma = 14f),
            primary = primary.withTone(if (dark) 78f else 42f),
            secondary = secondary.withTone(if (dark) 78f else 42f)
        )
    }
}

fun ThemeColors.toColorScheme(): ColorScheme {
    val dark = isDark
    val towardsEdge = if (dark) Color.Black else Color.White

    val primaryContainer = lerp(background, primary, if (dark) 0.3f else 0.2f)
    val secondaryContainer = lerp(background, secondary, if (dark) 0.3f else 0.2f)
    val onPrimaryContainer = lerp(primary, text, 0.4f)
    val onSecondaryContainer = lerp(secondary, text, 0.4f)

    return if (dark) darkColorScheme(
        primary = primary,
        onPrimary = readableTextOn(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = lerp(primary, Color.Black, 0.4f),
        secondary = secondary,
        onSecondary = readableTextOn(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = secondary,
        onTertiary = readableTextOn(secondary),
        tertiaryContainer = secondaryContainer,
        onTertiaryContainer = onSecondaryContainer,
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        surfaceVariant = lerp(surface, text, 0.1f),
        onSurfaceVariant = lerp(text, background, 0.3f),
        surfaceTint = primary,
        inverseSurface = text,
        inverseOnSurface = background,
        outline = outline ?: lerp(text, background, 0.5f),
        outlineVariant = lerp(outline ?: text, background, if (outline != null) 0.5f else 0.8f),
        surfaceDim = background,
        surfaceBright = lerp(surface, text, 0.15f),
        surfaceContainerLowest = lerp(background, towardsEdge, 0.3f),
        surfaceContainerLow = lerp(background, surface, 0.5f),
        surfaceContainer = surface,
        surfaceContainerHigh = lerp(surface, text, 0.06f),
        surfaceContainerHighest = lerp(surface, text, 0.12f)
    ) else lightColorScheme(
        primary = primary,
        onPrimary = readableTextOn(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = lerp(primary, Color.White, 0.4f),
        secondary = secondary,
        onSecondary = readableTextOn(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = secondary,
        onTertiary = readableTextOn(secondary),
        tertiaryContainer = secondaryContainer,
        onTertiaryContainer = onSecondaryContainer,
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        surfaceVariant = lerp(surface, text, 0.1f),
        onSurfaceVariant = lerp(text, background, 0.3f),
        surfaceTint = primary,
        inverseSurface = text,
        inverseOnSurface = background,
        outline = outline ?: lerp(text, background, 0.5f),
        outlineVariant = lerp(outline ?: text, background, if (outline != null) 0.5f else 0.8f),
        surfaceDim = lerp(background, Color.Black, 0.06f),
        surfaceBright = background,
        surfaceContainerLowest = lerp(background, towardsEdge, 0.5f),
        surfaceContainerLow = lerp(background, surface, 0.5f),
        surfaceContainer = surface,
        surfaceContainerHigh = lerp(surface, text, 0.06f),
        surfaceContainerHighest = lerp(surface, text, 0.12f)
    )
}

/** Near-black or near-white, slightly tinted towards the color it sits on. */
fun readableTextOn(color: Color): Color {
    val lch = color.toLch()
    return if (lch.lightness > 60f) lchToColor(12f, minOf(lch.chroma, 8f), lch.hue)
    else lchToColor(95f, minOf(lch.chroma, 6f), lch.hue)
}
