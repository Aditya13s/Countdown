package com.countdown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.countdown.databinding.ActivitySettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        loadCurrentSettings()

        binding.btnSaveSettings.setOnClickListener { saveSettings() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadCurrentSettings() {
        // App theme
        when (EventStorage.getThemeMode(this)) {
            "light" -> binding.rbThemeLight.isChecked = true
            "dark" -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        // Sort order
        when (EventStorage.getSortMode(this)) {
            "name" -> binding.rbSortName.isChecked = true
            else -> binding.rbSortDate.isChecked = true
        }

        // Widget font size
        if (EventStorage.isWidgetFontLarge(this)) {
            binding.rbFontLarge.isChecked = true
        } else {
            binding.rbFontNormal.isChecked = true
        }

        // Widget background style
        when (EventStorage.getWidgetBgStyle(this)) {
            "dark" -> binding.rbBgDark.isChecked = true
            "light" -> binding.rbBgLight.isChecked = true
            else -> binding.rbBgTeal.isChecked = true
        }

        // Reminder days
        when (EventStorage.getReminderDays(this)) {
            0 -> binding.rbReminderOff.isChecked = true
            3 -> binding.rbReminder3.isChecked = true
            7 -> binding.rbReminder7.isChecked = true
            else -> binding.rbReminder1.isChecked = true
        }
    }

    private fun saveSettings() {
        // App theme
        val themeMode = when (binding.rgTheme.checkedRadioButtonId) {
            R.id.rb_theme_light -> "light"
            R.id.rb_theme_dark -> "dark"
            else -> "system"
        }
        val previousTheme = EventStorage.getThemeMode(this)
        EventStorage.setThemeMode(this, themeMode)
        if (themeMode != previousTheme) {
            AppCompatDelegate.setDefaultNightMode(
                when (themeMode) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }

        // Sort order
        val sortMode = when (binding.rgSortOrder.checkedRadioButtonId) {
            R.id.rb_sort_name -> "name"
            else -> "date"
        }
        EventStorage.setSortMode(this, sortMode)

        // Widget font
        val fontLarge = binding.rgFontSize.checkedRadioButtonId == R.id.rb_font_large
        EventStorage.setWidgetFontLarge(this, fontLarge)

        // Widget background
        val bgStyle = when (binding.rgBgStyle.checkedRadioButtonId) {
            R.id.rb_bg_dark -> "dark"
            R.id.rb_bg_light -> "light"
            else -> "teal"
        }
        EventStorage.setWidgetBgStyle(this, bgStyle)

        // Reminder days
        val reminderDays = when (binding.rgReminder.checkedRadioButtonId) {
            R.id.rb_reminder_off -> 0
            R.id.rb_reminder_3 -> 3
            R.id.rb_reminder_7 -> 7
            else -> 1
        }
        val prevReminder = EventStorage.getReminderDays(this)
        EventStorage.setReminderDays(this, reminderDays)
        if (reminderDays != prevReminder) {
            NotificationHelper.rescheduleAll(this)
        }

        CountdownWidget.updateAllWidgets(this)
        Snackbar.make(binding.root, R.string.settings_saved, Snackbar.LENGTH_SHORT).show()
    }
}
