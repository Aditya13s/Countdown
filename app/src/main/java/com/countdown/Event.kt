package com.countdown

data class Event(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val dateMillis: Long,
    val colorIndex: Int = 0
) {
    fun daysRemaining(): Long {
        val now = System.currentTimeMillis()
        val diff = dateMillis - now
        return if (diff <= 0) 0L else (diff / (1000 * 60 * 60 * 24))
    }

    /** Returns (days, hours, minutes, seconds) until the event, all zeros if past. */
    fun timeComponents(): TimeComponents {
        val diff = (dateMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val totalSeconds = diff / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return TimeComponents(days, hours, minutes, seconds)
    }

    data class TimeComponents(val days: Long, val hours: Long, val minutes: Long, val seconds: Long) {
        val isZero: Boolean = days == 0L && hours == 0L && minutes == 0L && seconds == 0L
    }
}

/** Accent colours available for events (indices match AddEventActivity.EVENT_COLORS). */
val EVENT_COLORS = listOf(
    0xFF5C6BC0.toInt(),  // Indigo (default)
    0xFFE53935.toInt(),  // Red
    0xFF43A047.toInt(),  // Green
    0xFFFB8C00.toInt(),  // Orange
    0xFF8E24AA.toInt(),  // Purple
    0xFF00ACC1.toInt(),  // Cyan
    0xFFE91E63.toInt(),  // Pink
    0xFF00897B.toInt()   // Teal
)
