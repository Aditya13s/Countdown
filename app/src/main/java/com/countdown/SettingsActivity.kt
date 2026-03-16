package com.countdown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        // Font size
        if (EventStorage.isWidgetFontLarge(this)) {
            binding.rbFontLarge.isChecked = true
        } else {
            binding.rbFontNormal.isChecked = true
        }

        // Background style
        when (EventStorage.getWidgetBgStyle(this)) {
            "dark" -> binding.rbBgDark.isChecked = true
            "light" -> binding.rbBgLight.isChecked = true
            else -> binding.rbBgTeal.isChecked = true
        }
    }

    private fun saveSettings() {
        val fontLarge = binding.rgFontSize.checkedRadioButtonId == R.id.rb_font_large
        EventStorage.setWidgetFontLarge(this, fontLarge)

        val bgStyle = when (binding.rgBgStyle.checkedRadioButtonId) {
            R.id.rb_bg_dark -> "dark"
            R.id.rb_bg_light -> "light"
            else -> "teal"
        }
        EventStorage.setWidgetBgStyle(this, bgStyle)

        CountdownWidget.updateAllWidgets(this)
        Snackbar.make(binding.root, R.string.settings_saved, Snackbar.LENGTH_SHORT).show()
    }
}
