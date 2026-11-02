package com.zetroit.music.ui.styling

import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import com.zetroit.music.enums.MiniPlayerButtonStyle
import com.zetroit.music.enums.PlayPauseShape
import com.zetroit.music.enums.PlayPauseSize
import com.zetroit.music.enums.PlayPauseStyle
import com.zetroit.music.enums.ProgressBarThickness
import com.zetroit.music.utils.getEnum
import com.zetroit.music.utils.miniPlayerButtonStyleKey
import com.zetroit.music.utils.playPauseShapeKey
import com.zetroit.music.utils.playPauseSizeKey
import com.zetroit.music.utils.playPauseStyleKey
import com.zetroit.music.utils.progressBarThicknessKey
import com.zetroit.music.utils.showMiniPlayerProgressKey
import com.zetroit.music.utils.showTimeLabelsKey

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
