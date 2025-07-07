package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.R

@Composable
fun FuelLogsEntry(modifier: Modifier, onTap: () -> Unit, onLongPress: () -> Unit) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()
                    },
                    onTap = {
                        onTap()
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {

        Image(
            painter = painterResource(R.drawable.add_fuel),
            contentDescription = null,
            modifier = Modifier.padding(20.dp).size(56.dp)
        )
        /*
                Image(
                    painter = painterResource(R.drawable.add_fuel),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(20.dp)
                )*/
    }

}