package com.countdown

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class CountdownApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply Material You dynamic colors on Android 12+ (falls back to static theme on older)
        DynamicColors.applyToActivitiesIfAvailable(this)
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
