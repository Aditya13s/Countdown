package com.countdown

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (widgetId in appWidgetIds) {
            EventStorage.clearWidgetEventId(context, widgetId)
        }
    }

    companion object {
        // Font sizes (sp) for the "Large" font preference
        private const val FONT_LARGE_EMOJI_SP = 34f
        private const val FONT_LARGE_NAME_SP = 18f
        private const val FONT_LARGE_DAYS_SP = 56f
        private const val FONT_LARGE_LABEL_SP = 16f

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CountdownWidget::class.java)
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, CountdownWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Apply background based on settings
            val bgStyle = EventStorage.getWidgetBgStyle(context)
            val bgRes = when (bgStyle) {
                "dark" -> R.drawable.widget_background_dark
                "light" -> R.drawable.widget_background_light
                else -> R.drawable.widget_background
            }
            views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)

            // Apply font size scaling based on settings
            val fontLarge = EventStorage.isWidgetFontLarge(context)
            if (fontLarge) {
                views.setTextViewTextSize(R.id.widget_emoji, TypedValue.COMPLEX_UNIT_SP, FONT_LARGE_EMOJI_SP)
                views.setTextViewTextSize(R.id.widget_event_name, TypedValue.COMPLEX_UNIT_SP, FONT_LARGE_NAME_SP)
                views.setTextViewTextSize(R.id.widget_days_count, TypedValue.COMPLEX_UNIT_SP, FONT_LARGE_DAYS_SP)
                views.setTextViewTextSize(R.id.widget_days_label, TypedValue.COMPLEX_UNIT_SP, FONT_LARGE_LABEL_SP)
            }

            // For light background, switch text to dark colours so they are legible on white
            if (bgStyle == "light") {
                views.setTextColor(R.id.widget_event_name, ContextCompat.getColor(context, R.color.widget_text_on_light))
                views.setTextColor(R.id.widget_days_count, ContextCompat.getColor(context, R.color.widget_accent_on_light))
                views.setTextColor(R.id.widget_days_label, ContextCompat.getColor(context, R.color.widget_subtext_on_light))
                views.setTextColor(R.id.widget_event_date, ContextCompat.getColor(context, R.color.widget_subtext_on_light))
            }

            val event = EventStorage.getEventForWidget(context, widgetId)
            // Create a new instance per call — SimpleDateFormat is not thread-safe
            val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            if (event != null) {
                val days = event.daysRemaining()
                views.setTextViewText(R.id.widget_emoji, event.emoji ?: "🎯")
                views.setTextViewText(R.id.widget_event_name, event.name)
                views.setTextViewText(R.id.widget_days_count, days.toString())
                views.setTextViewText(
                    R.id.widget_days_label,
                    if (days == 1L) context.getString(R.string.day_left)
                    else context.getString(R.string.days_left)
                )
                views.setTextViewText(
                    R.id.widget_event_date,
                    dateFmt.format(Date(event.dateMillis))
                )
            } else {
                views.setTextViewText(R.id.widget_emoji, "⏳")
                views.setTextViewText(R.id.widget_event_name, context.getString(R.string.widget_no_event))
                views.setTextViewText(R.id.widget_days_count, "-")
                views.setTextViewText(R.id.widget_days_label, context.getString(R.string.days_left))
                views.setTextViewText(R.id.widget_event_date, "")
            }

            // Tap widget to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
