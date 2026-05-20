package com.ruchitech.carlanuchertab.clock

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.carlanuchertab.ui.theme.nonScaledSp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.ExperimentalTime

private val GoldLight = Color(0xFFF2E8D8)
private val Gold = Color(0xFFD4C4A8)
private val GoldDim = Color(0xFF8A7D68)
private val DialMid = Color(0xFF1A212C)
private val DialDark = Color(0xFF0A0E14)
private val NumberColor = Color(0xFFF5EFE6)
private val HandHour = Color(0xFFFAF7F2)
private val HandMinute = Color(0xFFE8EDF2)
private val HandSecond = Color(0xFFD4AF37)
private val AccentCyan = Color(0xFF5CE1E6)

private data class ClockFaceTime(
    val hourDegrees: Float,
    val minuteDegrees: Float,
    val secondDegrees: Float,
)

private data class ClockDisplayInfo(
    val faceTime: ClockFaceTime,
    val digitalTime: String,
    val dayName: String,
    val dateLine: String,
)

@Composable
fun ShowAnalogClock(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val clockSize = if (compact) 260.dp else 340.dp
    var display by remember { mutableStateOf(currentClockDisplay()) }

    DisposableEffect(Unit) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                display = currentClockDisplay()
                delay(1_000)
            }
        }
        onDispose { job.cancel() }
    }

    val digitalSize = if (compact) 40.sp.nonScaledSp else 48.sp.nonScaledSp
    val daySize = if (compact) 13.sp.nonScaledSp else 15.sp.nonScaledSp
    val dateSize = if (compact) 15.sp.nonScaledSp else 17.sp.nonScaledSp

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        NumberedAnalogClock(
            clockSize = clockSize,
            faceTime = display.faceTime
        )

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        ClockInfoPanel(
            digitalTime = display.digitalTime,
            dayName = display.dayName,
            dateLine = display.dateLine,
            digitalSize = digitalSize,
            daySize = daySize,
            dateSize = dateSize
        )
    }
}

@Composable
private fun ClockInfoPanel(
    digitalTime: String,
    dayName: String,
    dateLine: String,
    digitalSize: androidx.compose.ui.unit.TextUnit,
    daySize: androidx.compose.ui.unit.TextUnit,
    dateSize: androidx.compose.ui.unit.TextUnit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A222E).copy(alpha = 0.92f),
                        Color(0xFF10161F).copy(alpha = 0.88f)
                    )
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*Text(
            text = digitalTime,
            color = GoldLight,
            style = TextStyle(
                fontSize = digitalSize,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp
            ),
            textAlign = TextAlign.Center
        )*/
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Gold.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = dayName.uppercase(Locale.getDefault()),
            color = Gold,
            style = TextStyle(
                fontSize = daySize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = dateLine,
            color = Color(0xFFB8C5D3),
            style = TextStyle(
                fontSize = dateSize,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.8.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NumberedAnalogClock(
    clockSize: Dp,
    faceTime: ClockFaceTime,
) {
    Box(
        modifier = Modifier.size(clockSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(clockSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val scale = size.minDimension / 280f
            val bezelR = size.minDimension / 2f * 0.97f
            val dialR = bezelR * 0.84f

            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = bezelR + 5f * scale,
                center = center
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldLight.copy(alpha = 0.35f), GoldDim, Color(0xFF2A2520)),
                    center = center,
                    radius = bezelR
                ),
                radius = bezelR,
                center = center
            )

            drawCircle(
                color = Gold.copy(alpha = 0.45f),
                radius = bezelR,
                center = center,
                style = Stroke(width = 2.5f * scale)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(DialMid, Color(0xFF141A22), DialDark),
                    center = center,
                    radius = dialR
                ),
                radius = dialR,
                center = center
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = dialR * 0.94f,
                center = center,
                style = Stroke(width = 1f * scale)
            )

            for (tick in 0 until 60) {
                if (tick % 5 != 0) continue
                val angleRad = Math.toRadians((tick * 6 - 90).toDouble())
                val isHour = tick % 15 == 0
                val inner = dialR * if (isHour) 0.86f else 0.90f
                val outer = dialR * 0.96f
                drawLine(
                    color = if (isHour) Gold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                    start = Offset(
                        center.x + inner * cos(angleRad).toFloat(),
                        center.y + inner * sin(angleRad).toFloat()
                    ),
                    end = Offset(
                        center.x + outer * cos(angleRad).toFloat(),
                        center.y + outer * sin(angleRad).toFloat()
                    ),
                    strokeWidth = if (isHour) 2f * scale else 1f * scale,
                    cap = StrokeCap.Round
                )
            }

            val numberRadius = dialR * 0.70f
            val cardinalPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#F5EFE6")
                textSize = 26f * scale
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                setShadowLayer(5f * scale, 0f, 2f * scale, android.graphics.Color.BLACK)
            }
            val regularPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#E8DFD4")
                textSize = 20f * scale
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                setShadowLayer(4f * scale, 0f, 1.5f * scale, android.graphics.Color.BLACK)
            }

            for (hour in 1..12) {
                val isCardinal = hour % 3 == 0
                val paint = if (isCardinal) cardinalPaint else regularPaint
                val angleRad = Math.toRadians((hour * 30 - 90).toDouble())
                val x = center.x + numberRadius * cos(angleRad).toFloat()
                val y = center.y + numberRadius * sin(angleRad).toFloat() + 7f * scale
                drawContext.canvas.nativeCanvas.drawText(hour.toString(), x, y, paint)
            }

            drawTaperedHand(
                center, faceTime.hourDegrees, dialR * 0.44f,
                9f * scale, 2.5f * scale, 12f * scale, HandHour, GoldDim
            )
            drawTaperedHand(
                center, faceTime.minuteDegrees, dialR * 0.64f,
                6.5f * scale, 2f * scale, 16f * scale, HandMinute, GoldDim
            )
            drawSecondHand(center, faceTime.secondDegrees, dialR * 0.70f, scale)

            drawCircle(color = GoldDim, radius = 10f * scale, center = center)
            drawCircle(color = Gold, radius = 6f * scale, center = center)
            drawCircle(color = HandHour, radius = 2.5f * scale, center = center)
        }
    }
}

private fun DrawScope.drawTaperedHand(
    center: Offset,
    degrees: Float,
    length: Float,
    baseWidth: Float,
    tipWidth: Float,
    tailLength: Float,
    fillColor: Color,
    tailColor: Color,
) {
    rotate(degrees, pivot = center) {
        val handPath = Path().apply {
            moveTo(center.x - baseWidth / 2f, center.y)
            lineTo(center.x - tipWidth / 2f, center.y - length)
            lineTo(center.x + tipWidth / 2f, center.y - length)
            lineTo(center.x + baseWidth / 2f, center.y)
            close()
        }
        drawPath(handPath, color = fillColor, style = Fill)
        val tailPath = Path().apply {
            moveTo(center.x - baseWidth / 4f, center.y)
            lineTo(center.x + baseWidth / 4f, center.y)
            lineTo(center.x + tipWidth / 3f, center.y + tailLength)
            lineTo(center.x - tipWidth / 3f, center.y + tailLength)
            close()
        }
        drawPath(tailPath, color = tailColor, style = Fill)
    }
}

private fun DrawScope.drawSecondHand(
    center: Offset,
    degrees: Float,
    length: Float,
    scale: Float,
) {
    rotate(degrees, pivot = center) {
        drawLine(
            color = HandSecond,
            start = center,
            end = Offset(center.x, center.y - length),
            strokeWidth = 2f * scale,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = AccentCyan.copy(alpha = 0.9f),
            radius = 3.5f * scale,
            center = Offset(center.x, center.y - length)
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun currentClockDisplay(): ClockDisplayInfo {
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val nowDate = Date()

    val digitalTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(nowDate)
    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(nowDate)
    val dateLine = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(nowDate)

    return ClockDisplayInfo(
        faceTime = ClockFaceTime(
            hourDegrees = (now.hour % 12) * 30f + now.minute * 0.5f,
            minuteDegrees = now.minute * 6f + now.second * 0.1f,
            secondDegrees = now.second * 6f
        ),
        digitalTime = digitalTime,
        dayName = dayName,
        dateLine = dateLine
    )
}

/** @deprecated Use [ShowAnalogClock] instead. */
@Composable
fun AnalogClock(clockSize: Dp = 360.dp) {
    var display by remember { mutableStateOf(currentClockDisplay()) }

    DisposableEffect(Unit) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                display = currentClockDisplay()
                delay(1_000)
            }
        }
        onDispose { job.cancel() }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NumberedAnalogClock(clockSize = clockSize, faceTime = display.faceTime)
        Spacer(modifier = Modifier.height(14.dp))
        ClockInfoPanel(
            digitalTime = display.digitalTime,
            dayName = display.dayName,
            dateLine = display.dateLine,
            digitalSize = 48.sp.nonScaledSp,
            daySize = 15.sp.nonScaledSp,
            dateSize = 17.sp.nonScaledSp
        )
    }
}
