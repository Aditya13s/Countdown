package com.countdown

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.countdown.databinding.ActivityWidgetConfigBinding

class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedEventId = -1L
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Always set RESULT_CANCELED so the widget is not added if the user backs out
        setResult(RESULT_CANCELED)

        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.widget_config_title)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Restore previous selection
        val savedId = EventStorage.getWidgetEventId(this, appWidgetId)
        selectedEventId = savedId

        buildEventList()

        binding.btnConfigDone.setOnClickListener { confirmSelection() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun buildEventList() {
        val container = binding.llEventOptions
        radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // "Next upcoming event" option (id = -1 mapped as 0 in RadioButton id space)
        val nextOption = makeRadioButton(getString(R.string.widget_option_next), null, -1L)
        radioGroup.addView(nextOption)

        // One RadioButton per saved event
        val events = EventStorage.getEvents(this)
        for (event in events) {
            val label = "${event.emoji ?: "🎯"}  ${event.name}"
            val rb = makeRadioButton(label, EVENT_COLORS.getOrElse(event.colorIndex) { EVENT_COLORS[0] }, event.id)
            radioGroup.addView(rb)
        }

        // Select the previously saved option
        for (i in 0 until radioGroup.childCount) {
            val rb = radioGroup.getChildAt(i) as? RadioButton ?: continue
            val tag = rb.tag as? Long ?: continue
            if (tag == selectedEventId) {
                rb.isChecked = true
                break
            }
        }
        // Default to first item if nothing matched
        if (radioGroup.checkedRadioButtonId == -1 && radioGroup.childCount > 0) {
            (radioGroup.getChildAt(0) as? RadioButton)?.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            selectedEventId = rb?.tag as? Long ?: -1L
        }

        container.addView(radioGroup)
    }

    private fun makeRadioButton(label: String, accentColor: Int?, eventId: Long): RadioButton {
        return RadioButton(this).apply {
            text = label
            textSize = 15f
            tag = eventId
            gravity = Gravity.CENTER_VERTICAL
            val size = 6.dpToPx()
            setPadding(size, size, size, size)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 4.dpToPx() }

            if (accentColor != null) {
                val dot = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setSize(14.dpToPx(), 14.dpToPx())
                    setColor(accentColor)
                }
                setCompoundDrawablesWithIntrinsicBounds(null, null, dot, null)
                compoundDrawablePadding = 8.dpToPx()
            }
        }
    }

    private fun confirmSelection() {
        EventStorage.setWidgetEventId(this, appWidgetId, selectedEventId)
        CountdownWidget.updateWidget(this, AppWidgetManager.getInstance(this), appWidgetId)

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density + 0.5f).toInt()
}
