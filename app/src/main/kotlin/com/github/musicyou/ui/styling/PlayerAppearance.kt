package com.github.musicyou.ui.styling

import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import com.github.musicyou.enums.MiniPlayerButtonStyle
import com.github.musicyou.enums.PlayPauseShape
import com.github.musicyou.enums.PlayPauseSize
import com.github.musicyou.enums.PlayPauseStyle
import com.github.musicyou.enums.ProgressBarThickness
import com.github.musicyou.utils.getEnum
import com.github.musicyou.utils.miniPlayerButtonStyleKey
import com.github.musicyou.utils.playPauseShapeKey
import com.github.musicyou.utils.playPauseSizeKey
import com.github.musicyou.utils.playPauseStyleKey
import com.github.musicyou.utils.progressBarThicknessKey
import com.github.musicyou.utils.showMiniPlayerProgressKey
import com.github.musicyou.utils.showTimeLabelsKey

/** Appearance of the player controls; the defaults match the original look. */
@Immutable
data class PlayerAppearance(
    val progressBarThickness: ProgressBarThickness = ProgressBarThickness.Thin,
    val showTimeLabels: Boolean = true,
    val showMiniPlayerProgress: Boolean = true,
    val playPauseShape: PlayPauseShape = PlayPauseShape.Morphing,
    val playPauseSize: PlayPauseSize = PlayPauseSize.Default,
    val playPauseStyle: PlayPauseStyle = PlayPauseStyle.Filled,
    val miniPlayerButtonStyle: MiniPlayerButtonStyle = MiniPlayerButtonStyle.Plain
)

val LocalPlayerAppearance = compositionLocalOf { PlayerAppearance() }

internal val playerAppearanceKeys = setOf(
    progressBarThicknessKey,
    showTimeLabelsKey, showMiniPlayerProgressKey, playPauseShapeKey, playPauseSizeKey,
    playPauseStyleKey, miniPlayerButtonStyleKey
)

internal fun SharedPreferences.playerAppearance() = PlayerAppearance().let { defaults ->
    PlayerAppearance(
        progressBarThickness = getEnum(progressBarThicknessKey, defaults.progressBarThickness),
        showTimeLabels = getBoolean(showTimeLabelsKey, defaults.showTimeLabels),
        showMiniPlayerProgress = getBoolean(showMiniPlayerProgressKey, defaults.showMiniPlayerProgress),
        playPauseShape = getEnum(playPauseShapeKey, defaults.playPauseShape),
        playPauseSize = getEnum(playPauseSizeKey, defaults.playPauseSize),
        playPauseStyle = getEnum(playPauseStyleKey, defaults.playPauseStyle),
        miniPlayerButtonStyle = getEnum(miniPlayerButtonStyleKey, defaults.miniPlayerButtonStyle)
    )
}
