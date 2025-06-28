package com.ruchitech.carlanuchertab.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.ruchitech.carlanuchertab.clock.utill.Time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AnalogClock() {
    Box(
        modifier = Modifier.size(225.dp),
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

                    val currentMoment: Instant = Clock.System.now()
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

       /*     for (angle in 0..60) {
                rotate(angle * 6f) {
                    if (angle % 5 == 0) {
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, 40f),
                            strokeWidth = 8f
                        )
                    } else {
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width / 2, 15f),
                            end = Offset(size.width / 2, 25f),
                            strokeWidth = 8f
                        )
                    }
                }
            }*/

            for (hour in 0 until 12) {
                val angle = hour * 30f // 12 hours, each 30 degrees apart
                rotate(angle) {
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, 18f),
                        strokeWidth = 5f
                    )
                }
            }


      /*      rotate(time.hour) {
                drawLine(
                    color = Color.Yellow,
                    start = Offset(size.width / 2, size.width / 2),
                    end = Offset(size.width / 2, 150f),
                    strokeWidth = 14f
                )
            }*/

            rotate(degrees = time.hour , pivot = Offset(size.width / 2, size.height / 2)) {
                val center = Offset(size.width / 2, size.height / 2)

                val handLength = size.minDimension / 4
                val handWidth = 15f

                val path = Path().apply {
                    // Start at the top of the hand (pointed end)
                    moveTo(center.x, center.y - handLength) // Tip (points out)
                    lineTo(center.x - handWidth / 2, center.y) // Bottom-left (connected to center)
                    lineTo(center.x + handWidth / 2, center.y) // Bottom-right (connected to center)
                    close()
                }

                drawPath(path, color = Color.White)
            }



            val smoothMinuteAngle = (time.minute + time.second / 60f)
            rotate(degrees = smoothMinuteAngle, pivot = Offset(size.width / 2, size.height / 2)) {
                val center = Offset(size.width / 2, size.height / 2)
                val handLength = size.minDimension / 2.4f  // slightly shorter
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
            val handLength = size.minDimension / 2.1f
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