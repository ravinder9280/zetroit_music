package com.zetroit.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zetroit.music.R

/** The app's logo followed by its name, "Zetroit" bold and "Music" in the accent color. */
@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append("Zetroit") }
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.primary)) {
                    append("Music")
                }
            },
            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.5).sp),
            maxLines = 1
        )
    }
}
