package com.countdown

import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.countdown.databinding.ActivityAddEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private var selectedDateMillis: Long = 0L
    private var selectedColorIndex: Int = 0
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_event)

        setupColorPicker()
        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveEvent() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupColorPicker() {
        val colorViews = listOf(
            binding.color0, binding.color1, binding.color2, binding.color3,
            binding.color4, binding.color5, binding.color6, binding.color7
        )

        colorViews.forEachIndexed { index, view ->
            val circle = GradientDrawable()
            circle.shape = GradientDrawable.OVAL
            circle.setColor(EVENT_COLORS[index])
            view.background = circle

            view.setOnClickListener {
                val previous = selectedColorIndex
                selectedColorIndex = index
                if (previous != index) {
                    updateColorCircle(colorViews[previous], EVENT_COLORS[previous], selected = false)
                    updateColorCircle(colorViews[index], EVENT_COLORS[index], selected = true)
                }
            }
        }
        // Highlight default selection
        updateColorCircle(colorViews[0], EVENT_COLORS[0], selected = true)
    }

    private fun updateColorCircle(view: android.view.View, color: Int, selected: Boolean) {
        val circle = GradientDrawable()
        circle.shape = GradientDrawable.OVAL
        circle.setColor(color)
        if (selected) {
            circle.setStroke(
                4.dpToPx(),
                ContextCompat.getColor(this, R.color.color_picker_stroke)
            )
        }
        view.background = circle
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density + 0.5f).toInt()

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 23, 59, 59)
                cal.set(Calendar.MILLISECOND, 999)
                selectedDateMillis = cal.timeInMillis
                binding.tvSelectedDate.text = dateFormat.format(cal.time)
                binding.btnSave.isEnabled = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Minimum date = tomorrow
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        dialog.datePicker.minDate = tomorrow.timeInMillis
        dialog.show()
    }

    private fun saveEvent() {
        val name = binding.etEventName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilEventName.error = getString(R.string.error_enter_name)
            return
        }
        if (selectedDateMillis == 0L) {
            Toast.makeText(this, R.string.error_pick_date, Toast.LENGTH_SHORT).show()
            return
        }
        val event = Event(name = name, dateMillis = selectedDateMillis, colorIndex = selectedColorIndex)
        EventStorage.addEvent(this, event)
        CountdownWidget.updateAllWidgets(this)
        Toast.makeText(this, getString(R.string.event_added, name), Toast.LENGTH_SHORT).show()
        finish()
    }
}
