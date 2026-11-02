package com.zetroit.music.ui.styling

import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.zetroit.music.enums.AccentColor
import com.zetroit.music.enums.ColorSource
import com.zetroit.music.enums.ColorThemePreset
import com.zetroit.music.enums.CornerRoundness
import com.zetroit.music.enums.TextSize
import com.zetroit.music.enums.ThemeMode
import com.zetroit.music.utils.accentColorKey
import com.zetroit.music.utils.colorSourceKey
import com.zetroit.music.utils.colorThemeKey
import com.zetroit.music.utils.customBackgroundKey
import com.zetroit.music.utils.customPrimaryKey
import com.zetroit.music.utils.customSecondaryKey
import com.zetroit.music.utils.customSurfaceKey
import com.zetroit.music.utils.cornerRoundnessKey
import com.zetroit.music.utils.getEnum
import com.zetroit.music.utils.preferences
import com.zetroit.music.utils.pureBlackKey
import com.zetroit.music.utils.textSizeKey
import com.zetroit.music.utils.themeModeKey

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val appearance = rememberAppearance()

    val darkTheme = when (appearance.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorSource = ColorSource.effective(appearance.colorSource)

    val colorScheme = remember(
        darkTheme, colorSource, appearance.accentColor, appearance.colorTheme,
        appearance.customColors, appearance.pureBlack
    ) {
        val scheme = when {
            colorSource == ColorSource.Wallpaper && darkTheme -> dynamicDarkColorScheme(context)
            colorSource == ColorSource.Wallpaper -> dynamicLightColorScheme(context)
            colorSource == ColorSource.Theme -> appearance.colorTheme.colors(darkTheme).toColorScheme()
            colorSource == ColorSource.Custom -> appearance.customColors.adaptedTo(darkTheme).toColorScheme()
            appearance.accentColor != AccentColor.Indigo ->
                colorSchemeFromSeed(appearance.accentColor.seed, isDark = darkTheme)

            darkTheme -> darkScheme
            else -> lightScheme
        }
        if (darkTheme && appearance.pureBlack) scheme.withPureBlack() else scheme
    }

    // Scales the Material 3 default radii (4, 8, 12, 16 and 28 dp)
    val shapes = remember(appearance.cornerRoundness) {
        val scale = appearance.cornerRoundness.scale
        if (scale == 1f) Shapes()
        else Shapes(
            extraSmall = RoundedCornerShape(4.dp * scale),
            small = RoundedCornerShape(8.dp * scale),
            medium = RoundedCornerShape(12.dp * scale),
            large = RoundedCornerShape(16.dp * scale),
            extraLarge = RoundedCornerShape(28.dp * scale)
        )
    }

    val density = LocalDensity.current
    val scaledDensity = remember(density, appearance.textSize) {
        Density(density = density.density, fontScale = density.fontScale * appearance.textSize.scale)
    }

    // Status and navigation bar icons have to follow the app's theme, not the system's
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(activity, darkTheme) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
            ) { darkTheme }
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalPlayerAppearance provides appearance.player,
            content = content
        )
    }
}

@Immutable
private data class Appearance(
    val themeMode: ThemeMode,
    val pureBlack: Boolean,
    val colorSource: ColorSource,
    val accentColor: AccentColor,
    val colorTheme: ColorThemePreset,
    val customColors: ThemeColors,
    val textSize: TextSize,
    val cornerRoundness: CornerRoundness,
    val player: PlayerAppearance
)

private val appearanceKeys = setOf(
    themeModeKey, pureBlackKey, colorSourceKey, accentColorKey, textSizeKey, cornerRoundnessKey,
    colorThemeKey, customBackgroundKey, customSurfaceKey, customPrimaryKey, customSecondaryKey
)

/** Colors a custom theme starts with before the user changes any; the text color is worked out. */
val defaultCustomColors = ColorThemePreset.Nord.dark.let {
    ThemeColors(background = it.background, surface = it.surface, primary = it.primary, secondary = it.secondary)
}

private fun SharedPreferences.customColors() = ThemeColors(
    background = Color(getInt(customBackgroundKey, defaultCustomColors.background.toArgb())),
    surface = Color(getInt(customSurfaceKey, defaultCustomColors.surface.toArgb())),
    primary = Color(getInt(customPrimaryKey, defaultCustomColors.primary.toArgb())),
    secondary = Color(getInt(customSecondaryKey, defaultCustomColors.secondary.toArgb()))
)

private fun SharedPreferences.appearance() = Appearance(
    themeMode = getEnum(themeModeKey, ThemeMode.System),
    pureBlack = getBoolean(pureBlackKey, false),
    colorSource = getEnum(colorSourceKey, ColorSource.Default),
    accentColor = getEnum(accentColorKey, AccentColor.Indigo),
    colorTheme = getEnum(colorThemeKey, ColorThemePreset.Default),
    customColors = customColors(),
    textSize = getEnum(textSizeKey, TextSize.Default),
    cornerRoundness = getEnum(cornerRoundnessKey, CornerRoundness.Rounded),
    player = playerAppearance()
)

/** Reads the appearance settings once and updates only when one of them changes. */
@Composable
private fun rememberAppearance(): Appearance {
    val context = LocalContext.current
    var appearance by remember { mutableStateOf(context.preferences.appearance()) }

    DisposableEffect(context) {
        val preferences = context.preferences
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in appearanceKeys || key in playerAppearanceKeys) appearance = preferences.appearance()
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return appearance
}
