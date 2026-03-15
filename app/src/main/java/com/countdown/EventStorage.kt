package com.countdown

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EventStorage {

    private const val PREFS_NAME = "countdown_prefs"
    private const val KEY_EVENTS = "events"
    private val gson = Gson()

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getEvents(context: Context): MutableList<Event> {
        val json = prefs(context).getString(KEY_EVENTS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Event>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveEvents(context: Context, events: List<Event>) {
        prefs(context).edit().putString(KEY_EVENTS, gson.toJson(events)).apply()
    }

    fun addEvent(context: Context, event: Event) {
        val events = getEvents(context)
        events.add(event)
        saveEvents(context, events)
    }

    fun deleteEvent(context: Context, eventId: Long) {
        val events = getEvents(context).filter { it.id != eventId }
        saveEvents(context, events)
    }

    fun getNextEvent(context: Context): Event? {
        return getEvents(context)
            .filter { it.daysRemaining() > 0 }
            .minByOrNull { it.dateMillis }
    }
}
