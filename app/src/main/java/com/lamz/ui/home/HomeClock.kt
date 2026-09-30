package com.lamz.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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

    // Efficiently update time once per minute (aligned to the 00-second mark)
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            val cal = Calendar.getInstance()
            val seconds = cal.get(Calendar.SECOND)
            val millis = cal.get(Calendar.MILLISECOND)
            val millisUntilNextMinute = ((60 - seconds) * 1000) - millis
            val delayTime = millisUntilNextMinute.coerceAtLeast(1000).toLong()
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick
            )
            .testTag("home_clock_widget"),
        horizontalAlignment = Alignment.Start
    ) {
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
}
