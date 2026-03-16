package com.countdown

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EventStorage {

    private const val PREFS_NAME = "countdown_prefs"
    private const val KEY_EVENTS = "events"
    private const val KEY_WIDGET_FONT_LARGE = "widget_font_large"
    private const val KEY_WIDGET_BG_STYLE = "widget_bg_style"
    private const val PREFIX_WIDGET_EVENT_ID = "widget_event_id_"
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

    // ── Per-widget event selection ────────────────────────────────────────────

    /** Returns the pinned event ID for [widgetId], or -1 if set to "next upcoming". */
    fun getWidgetEventId(context: Context, widgetId: Int): Long =
        prefs(context).getLong("$PREFIX_WIDGET_EVENT_ID$widgetId", -1L)

    /** Saves the pinned event ID for [widgetId]. Use -1 to mean "next upcoming event". */
    fun setWidgetEventId(context: Context, widgetId: Int, eventId: Long) {
        prefs(context).edit().putLong("$PREFIX_WIDGET_EVENT_ID$widgetId", eventId).apply()
    }

    /** Removes the pinned event preference when a widget is deleted. */
    fun clearWidgetEventId(context: Context, widgetId: Int) {
        prefs(context).edit().remove("$PREFIX_WIDGET_EVENT_ID$widgetId").apply()
    }

    /** Returns the event that a given widget should display, falling back to next upcoming. */
    fun getEventForWidget(context: Context, widgetId: Int): Event? {
        val pinnedId = getWidgetEventId(context, widgetId)
        if (pinnedId != -1L) {
            val pinned = getEvents(context).firstOrNull { it.id == pinnedId }
            if (pinned != null) return pinned
        }
        return getNextEvent(context)
    }

    // ── Widget appearance settings ────────────────────────────────────────────

    fun isWidgetFontLarge(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIDGET_FONT_LARGE, false)

    fun setWidgetFontLarge(context: Context, large: Boolean) {
        prefs(context).edit().putBoolean(KEY_WIDGET_FONT_LARGE, large).apply()
    }

    /** Returns the widget background style: "teal" (default), "dark", or "light". */
    fun getWidgetBgStyle(context: Context): String =
        prefs(context).getString(KEY_WIDGET_BG_STYLE, "teal") ?: "teal"

    fun setWidgetBgStyle(context: Context, style: String) {
        prefs(context).edit().putString(KEY_WIDGET_BG_STYLE, style).apply()
    }
}
