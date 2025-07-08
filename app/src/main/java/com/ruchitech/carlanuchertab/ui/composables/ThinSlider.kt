package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color.White,
    trackColor: Color = Color.White,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        interactionSource = interactionSource,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(thumbColor = thumbColor),
                modifier = Modifier.size(16.dp) // Smaller thumb
            )
        },
        track = { sliderPositions ->

            SliderDefaults.Track(
                colors = SliderDefaults.colors(
                    activeTrackColor = trackColor,
                    inactiveTrackColor = trackColor.copy(alpha = 0.5f)
                ),
                sliderState = sliderPositions,
                modifier = Modifier.height(3.dp) // Thin track
            )
        },
        modifier = modifier.height(24.dp) // Overall touch target remains accessible
    )
}

