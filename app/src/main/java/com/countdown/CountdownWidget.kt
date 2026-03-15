package com.countdown

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

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

    companion object {
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
            val nextEvent = EventStorage.getNextEvent(context)

            if (nextEvent != null) {
                val days = nextEvent.daysRemaining()
                views.setTextViewText(R.id.widget_event_name, nextEvent.name)
                views.setTextViewText(R.id.widget_days_count, days.toString())
                views.setTextViewText(
                    R.id.widget_days_label,
                    if (days == 1L) context.getString(R.string.day_left)
                    else context.getString(R.string.days_left)
                )
            } else {
                views.setTextViewText(R.id.widget_event_name, context.getString(R.string.widget_no_event))
                views.setTextViewText(R.id.widget_days_count, "-")
                views.setTextViewText(R.id.widget_days_label, context.getString(R.string.days_left))
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
