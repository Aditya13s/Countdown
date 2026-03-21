package com.countdown

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val calendarId: Long,
    var isSelected: Boolean = false
)
