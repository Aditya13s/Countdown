package com.countdown

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class CountdownApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyStoredTheme()
        NotificationHelper.createNotificationChannel(this)
    }

    fun applyStoredTheme() {
        val mode = EventStorage.getThemeMode(this)
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
