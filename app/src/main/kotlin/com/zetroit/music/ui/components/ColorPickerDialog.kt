package com.zetroit.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.zetroit.music.R

private val quickColors = listOf(
    0xFF1DB954, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF3949AB, 0xFF1E88E5,
    0xFF00ACC1, 0xFF00897B, 0xFF7CB342, 0xFFFDD835, 0xFFFB8C00, 0xFF6D4C41,
    0xFF121212, 0xFF424242, 0xFFF5F5F5, 0xFFFFFFFF
).map(::Color)

/** Lets the user pick any color with a saturation/brightness area, a hue bar or a hex code. */
@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }

    val color = Color.hsv(hue, saturation, brightness)
    // Only the six hex digits; the # is shown as a fixed prefix so typing never moves the cursor
    var hexText by remember { mutableStateOf(TextFieldValue(initialColor.toHex().removePrefix("#"))) }

    fun showHex(newColor: Color) {
        val digits = newColor.toHex().removePrefix("#")
        hexText = TextFieldValue(digits, selection = TextRange(digits.length))
    }

    fun setColor(newColor: Color, updateHex: Boolean = true) {
        val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(newColor.toArgb(), it) }
        // Keeps the hue when picking a grey, where it would otherwise jump to red
        if (hsv[1] > 0f && hsv[2] > 0f) hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        if (updateHex) showHex(newColor)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(color)
                    onDismiss()
                }
            ) {
                Text(text = stringResource(id = R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SaturationBrightnessArea(
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    onChange = { newSaturation, newBrightness ->
                        saturation = newSaturation
                        brightness = newBrightness
                        showHex(Color.hsv(hue, newSaturation, newBrightness))
                    }
                )

                HueBar(
                    hue = hue,
                    onChange = { newHue ->
                        hue = newHue
                        showHex(Color.hsv(newHue, saturation, brightness))
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )

                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { value ->
                            val digits = value.text.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                                .uppercase()
                                .take(6)
                            hexText = if (digits == value.text) value
                            else value.copy(text = digits, selection = TextRange(digits.length))

                            if (digits.length == 6) {
                                setColor(Color(0xFF000000 or digits.toLong(16)), updateHex = false)
                            }
                        },
                        label = { Text(text = stringResource(id = R.string.hex_code)) },
                        prefix = { Text(text = "#") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    quickColors.forEach { quickColor ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(quickColor)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable { setColor(quickColor) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SaturationBrightnessArea(
    hue: Float,
    saturation: Float,
    brightness: Float,
    onChange: (saturation: Float, brightness: Float) -> Unit
) {
    val currentOnChange by rememberUpdatedState(onChange)

    fun pick(offset: Offset, width: Int, height: Int) = currentOnChange(
        (offset.x / width).coerceIn(0f, 1f),
        1f - (offset.y / height).coerceIn(0f, 1f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .pointerInput(Unit) {
                detectTapGestures { pick(it, size.width, size.height) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ -> pick(change.position, size.width, size.height) }
            }
            .drawWithContent {
                drawContent()
                val center = Offset(saturation * size.width, (1f - brightness) * size.height)
                drawCircle(Color.White, radius = 10.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
                drawCircle(Color.Black, radius = 12.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            }
    )
}

@Composable
private fun HueBar(hue: Float, onChange: (Float) -> Unit) {
    val rainbow = remember { (0..6).map { Color.hsv(it * 60f % 360f, 1f, 1f) } }

    val currentOnChange by rememberUpdatedState(onChange)

    fun pick(x: Float, width: Int) = currentOnChange((x / width).coerceIn(0f, 1f) * 359.9f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(rainbow))
            .pointerInput(Unit) {
                detectTapGestures { pick(it.x, size.width) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ -> pick(change.position.x, size.width) }
            }
            .drawWithContent {
                drawContent()
                val center = Offset(hue / 360f * size.width, size.height / 2)
                drawCircle(Color.White, radius = size.height / 2 - 2.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
            }
    )
}

fun Color.toHex() = "#%06X".format(toArgb() and 0xFFFFFF)
