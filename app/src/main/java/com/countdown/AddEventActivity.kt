package com.countdown

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.countdown.databinding.ActivityAddEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private var selectedDateMillis: Long = 0L
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_event)

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveEvent() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

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
        val event = Event(name = name, dateMillis = selectedDateMillis)
        EventStorage.addEvent(this, event)
        CountdownWidget.updateAllWidgets(this)
        Toast.makeText(this, getString(R.string.event_added, name), Toast.LENGTH_SHORT).show()
        finish()
    }
}
