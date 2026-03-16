package com.countdown

data class Event(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val dateMillis: Long
) {
    fun daysRemaining(): Long {
        val now = System.currentTimeMillis()
        val diff = dateMillis - now
        return if (diff <= 0) 0L else (diff / (1000 * 60 * 60 * 24))
    }
}
