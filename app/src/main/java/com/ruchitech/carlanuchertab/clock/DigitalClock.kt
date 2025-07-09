package com.ruchitech.carlanuchertab.clock

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.clock.utill.toAnalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun DigitalClock() {
    var time by remember {
        mutableStateOf("")
    }

    DisposableEffect(key1 = 0) {

        val job =
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    val currentMoment: kotlin.time.Instant = kotlin.time.Clock.System.now()
                    val calendar = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

                    time = calendar.time.toAnalog()

                    delay(1000)
                }
            }
        onDispose {
            job.cancel()
        }

    }

    Text(
        text = time,
        style = TextStyle(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            fontSize = 30.sp
        )
    )
}