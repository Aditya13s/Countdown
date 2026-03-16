package com.countdown

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID = "countdown_reminders"
    const val CHANNEL_NAME = "Event Reminders"
    const val EXTRA_EVENT_NAME = "event_name"
    const val EXTRA_EVENT_EMOJI = "event_emoji"
    const val EXTRA_DAYS_LEFT = "days_left"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your upcoming countdown events"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, event: Event, reminderDays: Int) {
        if (reminderDays <= 0) return
        val triggerTime = event.dateMillis - (reminderDays * 24L * 60 * 60 * 1000)
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_NAME, event.name)
            putExtra(EXTRA_EVENT_EMOJI, event.emoji ?: "🎯")
            putExtra(EXTRA_DAYS_LEFT, reminderDays)
        }
        val pi = PendingIntent.getBroadcast(
            context, (event.id % Int.MAX_VALUE).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
    }

    fun cancelReminder(context: Context, eventId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, (eventId % Int.MAX_VALUE).toInt(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
            pi.cancel()
        }
    }

    fun rescheduleAll(context: Context) {
        val reminderDays = EventStorage.getReminderDays(context)
        if (reminderDays <= 0) return
        EventStorage.getEvents(context).forEach { event ->
            scheduleReminder(context, event, reminderDays)
        }
    }
}
