package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog

@Composable
fun FuelLogsEntry(modifier: Modifier,onTap:()->Unit,onLongPress:()->Unit){
    Box(
        modifier = modifier
            .size(100.dp)
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
            modifier = Modifier
                .size(80.dp)
                .padding(20.dp)
        )
    }

}