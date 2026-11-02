package com.zetroit.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoSizeSelectSmall
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.SmartButton
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.R
import com.zetroit.music.enums.AccentColor
import com.zetroit.music.enums.ColorSource
import com.zetroit.music.enums.ColorThemePreset
import com.zetroit.music.enums.CornerRoundness
import com.zetroit.music.enums.NavigationLabelsVisibility
import com.zetroit.music.enums.TextSize
import com.zetroit.music.enums.ThemeMode
import com.zetroit.music.ui.components.ColorPickerDialog
import com.zetroit.music.ui.components.ValueSelectorDialog
import com.zetroit.music.ui.components.toHex
import com.zetroit.music.ui.styling.PlayerAppearance
import com.zetroit.music.ui.styling.ThemeColors
import com.zetroit.music.ui.styling.defaultCustomColors
import com.zetroit.music.utils.accentColorKey
import com.zetroit.music.utils.colorSourceKey
import com.zetroit.music.utils.colorThemeKey
import com.zetroit.music.utils.customBackgroundKey
import com.zetroit.music.utils.customPrimaryKey
import com.zetroit.music.utils.customSecondaryKey
import com.zetroit.music.utils.customSurfaceKey
import com.zetroit.music.utils.cornerRoundnessKey
import com.zetroit.music.utils.isAtLeastAndroid12
import com.zetroit.music.utils.isAtLeastAndroid13
import com.zetroit.music.utils.isShowingThumbnailInLockscreenKey
import com.zetroit.music.utils.miniPlayerButtonStyleKey
import com.zetroit.music.utils.navigationLabelsVisibilityKey
import com.zetroit.music.utils.playPauseShapeKey
import com.zetroit.music.utils.playPauseSizeKey
import com.zetroit.music.utils.playPauseStyleKey
import com.zetroit.music.utils.progressBarThicknessKey
import com.zetroit.music.utils.pureBlackKey
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.utils.showMiniPlayerProgressKey
import com.zetroit.music.utils.showTimeLabelsKey
import com.zetroit.music.utils.textSizeKey
import com.zetroit.music.utils.themeModeKey

@Composable
fun AppearanceSettings() {
    val playerPadding = LocalPlayerPadding.current

    var themeMode by rememberPreference(themeModeKey, ThemeMode.System)
    var pureBlack by rememberPreference(pureBlackKey, false)
    var colorSource by rememberPreference(colorSourceKey, ColorSource.Default)
    var accentColor by rememberPreference(accentColorKey, AccentColor.Indigo)
    var colorTheme by rememberPreference(colorThemeKey, ColorThemePreset.Default)
    var textSize by rememberPreference(textSizeKey, TextSize.Default)
    var cornerRoundness by rememberPreference(cornerRoundnessKey, CornerRoundness.Rounded)
    var navigationLabelsVisibility by rememberPreference(
        navigationLabelsVisibilityKey,
        NavigationLabelsVisibility.Visible
    )
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        false
    )

    val defaults = PlayerAppearance()
    var progressBarThickness by rememberPreference(progressBarThicknessKey, defaults.progressBarThickness)
    var showTimeLabels by rememberPreference(showTimeLabelsKey, defaults.showTimeLabels)
    var showMiniPlayerProgress by rememberPreference(showMiniPlayerProgressKey, defaults.showMiniPlayerProgress)
    var playPauseShape by rememberPreference(playPauseShapeKey, defaults.playPauseShape)
    var playPauseSize by rememberPreference(playPauseSizeKey, defaults.playPauseSize)
    var playPauseStyle by rememberPreference(playPauseStyleKey, defaults.playPauseStyle)
    var miniPlayerButtonStyle by rememberPreference(miniPlayerButtonStyleKey, defaults.miniPlayerButtonStyle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp + playerPadding)
    ) {
        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.theme),
            selectedValue = themeMode,
            onValueSelected = { themeMode = it },
            icon = Icons.Outlined.DarkMode,
            valueText = { stringResource(id = it.resourceId) }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.pure_black),
            text = stringResource(id = R.string.pure_black_description),
            icon = Icons.Outlined.Contrast,
            isChecked = pureBlack,
            onCheckedChange = { pureBlack = it },
            isEnabled = themeMode != ThemeMode.Light
        )

        val effectiveColorSource = ColorSource.effective(colorSource)

        ValueSelectorSettingsEntry(
            title = stringResource(id = R.string.color_source),
            selectedValue = effectiveColorSource,
            // Wallpaper colors only exist on Android 12 and newer
            values = ColorSource.entries.filter { isAtLeastAndroid12 || it != ColorSource.Wallpaper },
            onValueSelected = { colorSource = it },
            icon = Icons.Outlined.Palette,
            valueText = { stringResource(id = it.resourceId) }
        )

        when (effectiveColorSource) {
            ColorSource.Wallpaper -> Unit

            ColorSource.Preset -> {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.accent_color)) },
                    supportingContent = { Text(text = stringResource(id = accentColor.resourceId)) },
                    leadingContent = { Icon(imageVector = Icons.Outlined.Palette, contentDescription = null) }
                )

                AccentColorSelector(
                    selectedColor = accentColor,
                    onColorSelected = { accentColor = it }
                )
            }

            ColorSource.Theme -> {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.color_theme)) },
                    supportingContent = { Text(text = colorTheme.displayName) },
                    leadingContent = { Icon(imageVector = Icons.Outlined.ColorLens, contentDescription = null) }
                )

                ColorThemeSelector(
                    selectedTheme = colorTheme,
                    onThemeSelected = { colorTheme = it }
                )
            }

            ColorSource.Custom -> CustomColorsEditor()
        }

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.text_size),
            selectedValue = textSize,
            onValueSelected = { textSize = it },
            icon = Icons.Outlined.FormatSize,
            valueText = { stringResource(id = it.resourceId) }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.corner_roundness),
            selectedValue = cornerRoundness,
            onValueSelected = { cornerRoundness = it },
            icon = Icons.Outlined.RoundedCorner,
            valueText = { stringResource(id = it.resourceId) }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.navigation_bar_label_visibility),
            selectedValue = navigationLabelsVisibility,
            onValueSelected = { navigationLabelsVisibility = it },
            icon = Icons.Outlined.Visibility,
            valueText = { stringResource(id = it.resourceId) }
        )

        if (!isAtLeastAndroid13) {
            SwitchSettingEntry(
                title = stringResource(id = R.string.show_song_cover),
                text = stringResource(id = R.string.show_song_cover_description),
                icon = Icons.Outlined.Image,
                isChecked = isShowingThumbnailInLockscreen,
                onCheckedChange = { isShowingThumbnailInLockscreen = it }
            )
        }

        SettingsSectionTitle(text = stringResource(id = R.string.progress_bar))

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.thickness),
            selectedValue = progressBarThickness,
            onValueSelected = { progressBarThickness = it },
            icon = Icons.Outlined.LineWeight,
            valueText = { stringResource(id = it.resourceId) }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.time_labels),
            text = stringResource(id = R.string.time_labels_description),
            icon = Icons.Outlined.Timer,
            isChecked = showTimeLabels,
            onCheckedChange = { showTimeLabels = it }
        )

        SwitchSettingEntry(
            title = stringResource(id = R.string.mini_player_progress),
            text = stringResource(id = R.string.mini_player_progress_description),
            icon = Icons.Outlined.HorizontalRule,
            isChecked = showMiniPlayerProgress,
            onCheckedChange = { showMiniPlayerProgress = it }
        )

        SettingsSectionTitle(text = stringResource(id = R.string.play_pause_button))

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.shape),
            selectedValue = playPauseShape,
            onValueSelected = { playPauseShape = it },
            icon = Icons.Outlined.Category,
            valueText = { stringResource(id = it.resourceId) }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.button_size),
            selectedValue = playPauseSize,
            onValueSelected = { playPauseSize = it },
            icon = Icons.Outlined.PhotoSizeSelectSmall,
            valueText = { stringResource(id = it.resourceId) }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.style),
            selectedValue = playPauseStyle,
            onValueSelected = { playPauseStyle = it },
            icon = Icons.Outlined.PlayCircle,
            valueText = { stringResource(id = it.resourceId) }
        )

        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.mini_player_button),
            selectedValue = miniPlayerButtonStyle,
            onValueSelected = { miniPlayerButtonStyle = it },
            icon = Icons.Outlined.SmartButton,
            valueText = { stringResource(id = it.resourceId) }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun ColorThemeSelector(
    selectedTheme: ColorThemePreset,
    onThemeSelected: (ColorThemePreset) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 72.dp, end = 16.dp, bottom = 8.dp)
    ) {
        ColorThemePreset.entries.forEach { theme ->
            val colors = theme.colors(isDark)
            val isSelected = theme == selectedTheme

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onThemeSelected(theme) }
                    .padding(4.dp)
            ) {
                // A miniature of the app: background, a card in the surface color and both accents
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 64.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.background)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 10.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(colors.text)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(colors.surface)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colors.primary))
                        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colors.secondary))
                    }
                }

                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun CustomColorsEditor() {
    var background by rememberPreference(customBackgroundKey, defaultCustomColors.background.toArgb())
    var surface by rememberPreference(customSurfaceKey, defaultCustomColors.surface.toArgb())
    var primary by rememberPreference(customPrimaryKey, defaultCustomColors.primary.toArgb())
    var secondary by rememberPreference(customSecondaryKey, defaultCustomColors.secondary.toArgb())

    var isChoosingPreset by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    fun setColors(colors: ThemeColors) {
        background = colors.background.toArgb()
        surface = colors.surface.toArgb()
        primary = colors.primary.toArgb()
        secondary = colors.secondary.toArgb()
    }

    if (isChoosingPreset) {
        ValueSelectorDialog(
            onDismiss = { isChoosingPreset = false },
            title = stringResource(id = R.string.start_from_preset),
            selectedValue = null,
            values = ColorThemePreset.entries.toList(),
            onValueSelected = { preset -> preset?.let { setColors(it.colors(isDark)) } },
            valueText = { it?.displayName.orEmpty() }
        )
    }

    CustomColorEntry(
        title = stringResource(id = R.string.background_color),
        color = Color(background),
        onColorSelected = { background = it.toArgb() }
    )

    CustomColorEntry(
        title = stringResource(id = R.string.surface_color),
        color = Color(surface),
        onColorSelected = { surface = it.toArgb() }
    )

    CustomColorEntry(
        title = stringResource(id = R.string.primary_color),
        color = Color(primary),
        onColorSelected = { primary = it.toArgb() }
    )

    CustomColorEntry(
        title = stringResource(id = R.string.secondary_color),
        color = Color(secondary),
        onColorSelected = { secondary = it.toArgb() }
    )

    SettingsEntry(
        title = stringResource(id = R.string.start_from_preset),
        text = stringResource(id = R.string.start_from_preset_description),
        icon = Icons.Outlined.ColorLens,
        onClick = { isChoosingPreset = true }
    )

    SettingsEntry(
        title = stringResource(id = R.string.reset_colors),
        text = stringResource(id = R.string.reset_colors_description),
        icon = Icons.Outlined.RestartAlt,
        onClick = { setColors(defaultCustomColors) }
    )
}

@Composable
private fun CustomColorEntry(
    title: String,
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    var isPicking by remember { mutableStateOf(false) }

    if (isPicking) {
        ColorPickerDialog(
            title = title,
            initialColor = color,
            onDismiss = { isPicking = false },
            onColorSelected = onColorSelected
        )
    }

    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = color.toHex()) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        },
        modifier = Modifier.clickable { isPicking = true }
    )
}

@Composable
private fun AccentColorSelector(
    selectedColor: AccentColor,
    onColorSelected: (AccentColor) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 72.dp, end = 16.dp, bottom = 8.dp)
    ) {
        AccentColor.entries.forEach { accentColor ->
            val isSelected = accentColor == selectedColor

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.seed)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        ) else Modifier
                    )
                    .clickable { onColorSelected(accentColor) }
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(id = accentColor.resourceId),
                        tint = if (accentColor.seed.luminance() > 0.5f) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
