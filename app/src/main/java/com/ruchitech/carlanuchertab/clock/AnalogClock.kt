package com.ruchitech.carlanuchertab.clock

import android.R.attr.top
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.clock.utill.Time
import com.ruchitech.carlanuchertab.helper.getCurrentDateFormatted
import com.ruchitech.carlanuchertab.ui.theme.nonScaledSp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.ExperimentalTime


@Composable
fun ShowAnalogClock(modifier: Modifier) {
    Box(modifier){
        AnalogClock()
        Box(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .padding(top = 80.dp)

        ) {
            Text(
                text = getCurrentDateFormatted(), style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    fontSize = 24.sp.nonScaledSp
                )
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AnalogClock() {
    Box(
        modifier = Modifier.padding(15.dp).size(380.dp),
        contentAlignment = Alignment.Center
    ) {
        var time by remember {
            mutableStateOf(
                Time(
                    hour = 0f,
                    minute = 0f,
                    second = 0f
                )
            )
        }

        DisposableEffect(key1 = 0) {

            val job = CoroutineScope(Dispatchers.IO).launch {
                while (true) {

                    val currentMoment: kotlin.time.Instant = kotlin.time.Clock.System.now()
                    val calendar = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

                    val hour = calendar.hour
                    val minute = calendar.minute
                    val second = calendar.second

                    time = Time(
                        hour = ((hour + (minute / 60f)) * 6f * 5),
                        minute = minute * 6f,
                        second = second * 6f
                    )

                    delay(1000)
                }
            }

            onDispose {
                job.cancel()
            }
        }

        Canvas(modifier = Modifier.size(250.dp)) {
            val radius = size.minDimension / 2.5f // You can adjust this radius as needed
            val center2 = Offset(size.width / 2, size.height / 2)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 40f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            for (hour in 1..12) {
                val angleRad = Math.toRadians((hour * 30 - 90).toDouble()) // Rotate -90 to start from top
                val x = center2.x + (radius * cos(angleRad)).toFloat()
                val y = center2.y + (radius * sin(angleRad)).toFloat()

                drawContext.canvas.nativeCanvas.drawText(
                    hour.toString(),
                    x,
                    y + 15f, // small vertical offset for better alignment
                    textPaint
                )
            }

            // Hour hand with 3D effect
            rotate(time.hour, pivot = center2) {
                val path = Path().apply {
                    moveTo(center2.x, center2.y - size.minDimension / 4)
                    lineTo(center2.x - 8f, center2.y)
                    lineTo(center2.x, center2.y + 10f)
                    lineTo(center2.x + 8f, center2.y)
                    close()
                }
                drawPath(
                    path,
                    color = Color.White,
                    style = Fill
                )
                drawPath(
                    path,
                    color = Color(0xFF3A5A78),
                    style = Stroke(width = 1f)
                )
            }



            val smoothMinuteAngle = (time.minute + time.second / 60f)
            rotate(degrees = smoothMinuteAngle, pivot = Offset(size.width / 2, size.height / 2)) {
                val center = Offset(size.width / 2, size.height / 2)
                val handLength = size.minDimension / 2.9f  // slightly shorter
                val handWidth = 6f                        // slightly wider

                val path = Path().apply {
                    moveTo(center.x, center.y - handLength) // Tip (outer side)
                    lineTo(center.x - handWidth, center.y + 5f)  // Lower left
                    lineTo(center.x + handWidth, center.y + 5f)  // Lower right
                    close()
                }

                drawPath(path, color = Color.White)
            }


            val center = Offset(size.width / 2, size.height / 2)
            val handLength = size.minDimension / 2.5f
            val handWidth = 6f

            rotate(degrees = time.second , pivot = center) {
                val path = Path().apply {
                    moveTo(center.x, center.y - handLength) // Tip of the hand
                    lineTo(center.x - handWidth, center.y + 20f) // Left base (short tail)
                    lineTo(center.x + handWidth, center.y + 20f) // Right base
                    close()
                }

                drawPath(path, color = Color(0xFFD50000))
            }


            drawCircle(
                color = Color.White,
                radius = 12f
            )
        }
    }
}