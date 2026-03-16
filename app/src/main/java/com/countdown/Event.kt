package com.countdown

data class Event(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val dateMillis: Long,
    val colorIndex: Int = 0,
    /** Optional user note / description (null for events created before this field was added). */
    val note: String? = null,
    /** Emoji badge chosen by the user (null → default "🎯"). */
    val emoji: String? = null,
    /** Unix-ms when the event was created; 0 when unknown (migrated from older data). */
    val createdAt: Long = 0L
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

    /**
     * Returns 0-100 representing how far along we are between creation and event date.
     * Returns 0 when [createdAt] is unknown (legacy events).
     * Returns 100 when the event has already passed.
     */
    fun progressPercent(): Int {
        if (createdAt <= 0L) return 0
        val now = System.currentTimeMillis()
        if (now >= dateMillis) return 100
        val total = dateMillis - createdAt
        if (total <= 0) return 100
        val elapsed = now - createdAt
        return (elapsed * 100L / total).toInt().coerceIn(0, 100)
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

/** Emoji options for event badges. */
val EVENT_EMOJIS = listOf(
    "🎯", "🎂", "🎄", "🏆", "✈️", "🎓", "💍", "🏃",
    "🎵", "🏖️", "🎉", "💼", "❤️", "🎁", "📅", "🚀",
    "🌟", "🔔", "🍕", "⚽"
)
