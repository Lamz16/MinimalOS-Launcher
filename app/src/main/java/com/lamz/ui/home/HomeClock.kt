package com.lamz.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeClock(
    showClock: Boolean,
    showDate: Boolean,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    onClockClick: () -> Unit = {}
) {
    if (!showClock && !showDate) return

    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    // Update once per second so the analog clock remains real-time.
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            val cal = Calendar.getInstance()
            val millis = cal.get(Calendar.MILLISECOND)
            val delayTime = (1_000 - millis).coerceAtLeast(16).toLong()
            delay(delayTime)
        }
    }

    val timeFormat = remember(is24Hour) {
        if (is24Hour) SimpleDateFormat("HH:mm", Locale.getDefault())
        else SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val dateFormat = remember {
        SimpleDateFormat("EEEE\nd MMMM", Locale.getDefault())
    }

    val timeString = remember(currentTime, timeFormat) {
        timeFormat.format(currentTime)
    }

    val dateString = remember(currentTime, dateFormat) {
        dateFormat.format(currentTime)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick
            )
            .testTag("home_clock_widget"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (showClock) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (showDate) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showClock) {
            AnalogClock(currentTime)
        }
    }
}

@Composable
private fun AnalogClock(time: Date) {
    val calendar = remember(time) { Calendar.getInstance().apply { this.time = time } }
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = Modifier.size(68.dp)) {
        val radius = size.minDimension / 2f
        val clockCenter = this.center
        drawCircle(
            color = outline.copy(alpha = 0.65f),
            radius = radius - 1.dp.toPx(),
            center = clockCenter,
            style = Stroke(1.dp.toPx())
        )

        repeat(12) { index ->
            val angle = (index * 30f - 90f) * PI.toFloat() / 180f
            val outer = radius - 6.dp.toPx()
            val inner = outer - if (index % 3 == 0) 5.dp.toPx() else 3.dp.toPx()
            drawLine(
                color = outline,
                start = pointOnClock(clockCenter, angle, inner),
                end = pointOnClock(clockCenter, angle, outer),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        fun hand(angleDegrees: Float, lengthRatio: Float, width: Float, color: Color) {
            val angle = (angleDegrees - 90f) * PI.toFloat() / 180f
            drawLine(
                color = color,
                start = clockCenter,
                end = pointOnClock(clockCenter, angle, radius * lengthRatio),
                strokeWidth = width,
                cap = StrokeCap.Round
            )
        }

        hand((hour % 12) * 30f + minute * 0.5f, 0.45f, 3.dp.toPx(), primary)
        hand(minute * 6f + second * 0.1f, 0.67f, 2.dp.toPx(), primary)
        hand(second * 6f, 0.74f, 1.dp.toPx(), secondary)
        drawCircle(primary, 3.dp.toPx(), clockCenter)
    }
}

private fun pointOnClock(
    center: androidx.compose.ui.geometry.Offset,
    angle: Float,
    distance: Float
): androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset(
    center.x + cos(angle.toDouble()).toFloat() * distance,
    center.y + sin(angle.toDouble()).toFloat() * distance
)
