package com.zetroit.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zetroit.music.LocalPlayerPadding
import com.zetroit.music.R
import com.zetroit.music.enums.QuickPicksSource
import com.zetroit.music.utils.isAtLeastAndroid12
import com.zetroit.music.utils.isAtLeastAndroid13
import com.zetroit.music.utils.quickPicksSourceKey
import com.zetroit.music.utils.rememberPreference
import com.zetroit.music.utils.toast

@Composable
fun GeneralSettings() {
    val playerPadding = LocalPlayerPadding.current

    val context = LocalContext.current
    var quickPicksSource by rememberPreference(quickPicksSourceKey, QuickPicksSource.Trending)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp + playerPadding)
    ) {
        EnumValueSelectorSettingsEntry(
            title = stringResource(id = R.string.quick_picks_source),
            selectedValue = quickPicksSource,
            onValueSelected = { quickPicksSource = it },
            icon = Icons.AutoMirrored.Outlined.List,
            valueText = { stringResource(id = it.resourceId) }
        )

        if (isAtLeastAndroid13) {
            val intent = Intent(
                Settings.ACTION_APP_LOCALE_SETTINGS,
                "package:${context.packageName}".toUri()
            )

            SettingsEntry(
                title = stringResource(id = R.string.app_language),
                text = stringResource(id = R.string.configure_app_language),
                icon = Icons.Outlined.Language,
                onClick = {
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        context.toast("Couldn't find app language settings, please configure them manually")
                    }
                }
            )
        }

        if (isAtLeastAndroid12) {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                "package:${context.packageName}".toUri()
            )

            SettingsEntry(
                title = stringResource(id = R.string.open_supported_links_by_default),
                text = stringResource(id = R.string.configure_supported_links),
                icon = Icons.Outlined.AddLink,
                onClick = {
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        context.toast("Couldn't find supported links settings, please configure them manually")
                    }
                }
            )
        }
    }
}