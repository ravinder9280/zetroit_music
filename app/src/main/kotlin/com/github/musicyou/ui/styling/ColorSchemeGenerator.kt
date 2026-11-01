package com.github.musicyou.ui.styling

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Builds a Material 3 color scheme from a single seed color, following the tonal
 * roles of the "tonal spot" style. Tones are CIE L* values, so contrast between
 * roles matches the default schemes. Only runs when the accent color changes.
 */
fun colorSchemeFromSeed(seed: Color, isDark: Boolean): ColorScheme {
    val hue = seed.lchHue()
    val primary = TonalPalette(hue, chroma = 48f)
    val secondary = TonalPalette(hue, chroma = 16f)
    val tertiary = TonalPalette(hue + 60f, chroma = 24f)
    val neutral = TonalPalette(hue, chroma = 4f)
    val neutralVariant = TonalPalette(hue, chroma = 8f)

    return if (isDark) darkColorScheme(
        primary = primary.tone(80),
        onPrimary = primary.tone(20),
        primaryContainer = primary.tone(30),
        onPrimaryContainer = primary.tone(90),
        inversePrimary = primary.tone(40),
        secondary = secondary.tone(80),
        onSecondary = secondary.tone(20),
        secondaryContainer = secondary.tone(30),
        onSecondaryContainer = secondary.tone(90),
        tertiary = tertiary.tone(80),
        onTertiary = tertiary.tone(20),
        tertiaryContainer = tertiary.tone(30),
        onTertiaryContainer = tertiary.tone(90),
        background = neutral.tone(6),
        onBackground = neutral.tone(90),
        surface = neutral.tone(6),
        onSurface = neutral.tone(90),
        surfaceVariant = neutralVariant.tone(30),
        onSurfaceVariant = neutralVariant.tone(80),
        surfaceTint = primary.tone(80),
        inverseSurface = neutral.tone(90),
        inverseOnSurface = neutral.tone(20),
        outline = neutralVariant.tone(60),
        outlineVariant = neutralVariant.tone(30),
        surfaceBright = neutral.tone(24),
        surfaceDim = neutral.tone(6),
        surfaceContainerLowest = neutral.tone(4),
        surfaceContainerLow = neutral.tone(10),
        surfaceContainer = neutral.tone(12),
        surfaceContainerHigh = neutral.tone(17),
        surfaceContainerHighest = neutral.tone(22)
    ) else lightColorScheme(
        primary = primary.tone(40),
        onPrimary = primary.tone(100),
        primaryContainer = primary.tone(90),
        onPrimaryContainer = primary.tone(10),
        inversePrimary = primary.tone(80),
        secondary = secondary.tone(40),
        onSecondary = secondary.tone(100),
        secondaryContainer = secondary.tone(90),
        onSecondaryContainer = secondary.tone(10),
        tertiary = tertiary.tone(40),
        onTertiary = tertiary.tone(100),
        tertiaryContainer = tertiary.tone(90),
        onTertiaryContainer = tertiary.tone(10),
        background = neutral.tone(98),
        onBackground = neutral.tone(10),
        surface = neutral.tone(98),
        onSurface = neutral.tone(10),
        surfaceVariant = neutralVariant.tone(90),
        onSurfaceVariant = neutralVariant.tone(30),
        surfaceTint = primary.tone(40),
        inverseSurface = neutral.tone(20),
        inverseOnSurface = neutral.tone(95),
        outline = neutralVariant.tone(50),
        outlineVariant = neutralVariant.tone(80),
        surfaceBright = neutral.tone(98),
        surfaceDim = neutral.tone(87),
        surfaceContainerLowest = neutral.tone(100),
        surfaceContainerLow = neutral.tone(96),
        surfaceContainer = neutral.tone(94),
        surfaceContainerHigh = neutral.tone(92),
        surfaceContainerHighest = neutral.tone(90)
    )
}

/** Replaces the dark backgrounds with pure black while keeping raised surfaces distinguishable. */
fun ColorScheme.withPureBlack() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0C0C0C),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF222222)
)

private class TonalPalette(private val hue: Float, private val chroma: Float) {
    fun tone(tone: Int): Color = lchToColor(tone.toFloat(), chroma, hue)
}

// CIE LCh(ab) <-> sRGB conversions with the D65 white point

private const val WHITE_X = 0.95047f
private const val WHITE_Z = 1.08883f

internal data class Lch(val lightness: Float, val chroma: Float, val hue: Float)

internal fun Color.toLch(): Lch {
    val x = (0.4124f * red.linear() + 0.3576f * green.linear() + 0.1805f * blue.linear()) / WHITE_X
    val y = 0.2126f * red.linear() + 0.7152f * green.linear() + 0.0722f * blue.linear()
    val z = (0.0193f * red.linear() + 0.1192f * green.linear() + 0.9505f * blue.linear()) / WHITE_Z

    val a = 500f * (x.labF() - y.labF())
    val b = 200f * (y.labF() - z.labF())
    return Lch(
        lightness = 116f * y.labF() - 16f,
        chroma = sqrt(a * a + b * b),
        hue = Math.toDegrees(atan2(b, a).toDouble()).toFloat()
    )
}

private fun Color.lchHue() = toLch().hue

/** Lowers the chroma until the color fits in sRGB, keeping its tone and hue. */
internal fun lchToColor(lightness: Float, chroma: Float, hue: Float): Color {
    if (lightness <= 0f) return Color.Black
    if (lightness >= 100f) return Color.White

    var low = 0f
    var high = chroma
    var best = labToRgb(lightness, 0f, 0f)!!

    repeat(12) {
        val mid = (low + high) / 2f
        val radians = Math.toRadians(hue.toDouble())
        val rgb = labToRgb(
            lightness,
            mid * cos(radians).toFloat(),
            mid * sin(radians).toFloat()
        )
        if (rgb != null) {
            best = rgb
            low = mid
        } else high = mid
    }

    return Color(best[0], best[1], best[2])
}

private fun labToRgb(lightness: Float, a: Float, b: Float): FloatArray? {
    val fy = (lightness + 16f) / 116f
    val x = WHITE_X * (fy + a / 500f).labFInverse()
    val y = fy.labFInverse()
    val z = WHITE_Z * (fy - b / 200f).labFInverse()

    val rgb = floatArrayOf(
        3.2406f * x - 1.5372f * y - 0.4986f * z,
        -0.9689f * x + 1.8758f * y + 0.0415f * z,
        0.0557f * x - 0.2040f * y + 1.0570f * z
    )

    for (i in rgb.indices) {
        val channel = rgb[i]
        if (channel < -0.0001f || channel > 1.0001f) return null
        rgb[i] = channel.coerceIn(0f, 1f).gamma()
    }
    return rgb
}

private fun Float.linear() = if (this <= 0.04045f) this / 12.92f else ((this + 0.055f) / 1.055f).pow(2.4f)

private fun Float.gamma() = if (this <= 0.0031308f) this * 12.92f else 1.055f * pow(1f / 2.4f) - 0.055f

private fun Float.labF() = if (this > 216f / 24389f) cbrt(this) else (24389f / 27f * this + 16f) / 116f

private fun Float.labFInverse(): Float {
    val cubed = this * this * this
    return if (cubed > 216f / 24389f) cubed else (116f * this - 16f) / (24389f / 27f)
}
