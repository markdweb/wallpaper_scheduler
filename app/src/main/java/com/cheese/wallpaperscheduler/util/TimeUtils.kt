package com.cheese.wallpaperscheduler.util

import com.cheese.wallpaperscheduler.data.TimeOfDay

/** "6:00 AM" / "5:00 PM" style formatting, used across Home/Schedule/Settings. */
fun TimeOfDay.formatted(): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, period)
}
