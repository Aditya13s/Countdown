package com.countdown

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_NAME) ?: return
        val emoji = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_EMOJI) ?: "🎯"
        val daysLeft = intent.getIntExtra(NotificationHelper.EXTRA_DAYS_LEFT, 1)

        val openIntent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = when {
            daysLeft <= 0 -> "$emoji Today is the day! 🎊"
            daysLeft == 1 -> "$emoji Tomorrow is the big day!"
            else -> "$emoji $daysLeft days to go! ⏳"
        }

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(eventName)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(eventName.hashCode(), notification)
    }
}
