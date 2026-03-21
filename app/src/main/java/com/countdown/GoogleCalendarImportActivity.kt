package com.countdown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.countdown.databinding.ActivityGoogleCalendarImportBinding
import com.google.android.material.snackbar.Snackbar

class GoogleCalendarImportActivity : AppCompatActivity() {

    private companion object {
        const val DEFAULT_IMPORT_COLOR_INDEX = 2
    }

    private lateinit var binding: ActivityGoogleCalendarImportBinding
    private val calendarEvents = mutableListOf<CalendarEvent>()
    private lateinit var adapter: CalendarEventAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) loadCalendarEvents() else showPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleCalendarImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.import_from_calendar)

        adapter = CalendarEventAdapter(calendarEvents) { updateImportButton() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnImport.setOnClickListener { importSelectedEvents() }
        binding.btnSelectAll.setOnClickListener { toggleSelectAll() }

        checkPermissionAndLoad()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadCalendarEvents()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    private fun loadCalendarEvents() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        val now = System.currentTimeMillis()
        val endTime = now + 365L * 24 * 60 * 60 * 1000

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.CALENDAR_ID
        )
        val selection = "(${CalendarContract.Events.DTSTART} >= ?) " +
                "AND (${CalendarContract.Events.DTSTART} <= ?) " +
                "AND (${CalendarContract.Events.DELETED} != 1)"
        val selectionArgs = arrayOf(now.toString(), endTime.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        try {
            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection, selection, selectionArgs, sortOrder
            )
            calendarEvents.clear()
            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val calIdIdx = it.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID)
                while (it.moveToNext()) {
                    val title = it.getString(titleIdx) ?: continue
                    if (title.isBlank()) continue
                    calendarEvents.add(
                        CalendarEvent(
                            id = it.getLong(idIdx),
                            title = title.trim(),
                            startMillis = it.getLong(startIdx),
                            calendarId = it.getLong(calIdIdx)
                        )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Snackbar.make(binding.root, getString(R.string.calendar_read_error), Snackbar.LENGTH_LONG).show()
        }

        binding.progressBar.visibility = View.GONE
        val empty = calendarEvents.isEmpty()
        binding.tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        updateImportButton()
    }

    private fun showPermissionDenied() {
        binding.progressBar.visibility = View.GONE
        binding.tvEmpty.text = getString(R.string.calendar_permission_denied)
        binding.tvEmpty.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.btnSelectAll.isEnabled = false
    }

    private fun updateImportButton() {
        val count = calendarEvents.count { it.isSelected }
        val allSelected = calendarEvents.isNotEmpty() && calendarEvents.all { it.isSelected }
        binding.btnImport.isEnabled = count > 0
        binding.btnImport.text = if (count > 0)
            getString(R.string.import_n_events, count)
        else
            getString(R.string.import_events)
        binding.btnSelectAll.text = if (allSelected)
            getString(R.string.deselect_all)
        else
            getString(R.string.select_all)
    }

    private fun toggleSelectAll() {
        val allSelected = calendarEvents.all { it.isSelected }
        calendarEvents.forEach { it.isSelected = !allSelected }
        adapter.notifyDataSetChanged()
        updateImportButton()
    }

    private fun importSelectedEvents() {
        val selected = calendarEvents.filter { it.isSelected }
        if (selected.isEmpty()) return
        val existing = EventStorage.getEvents(this)
        val reminderDays = EventStorage.getReminderDays(this)
        var imported = 0
        var skipped = 0
        selected.forEach { calEvent ->
            val duplicate = existing.any {
                it.name.equals(calEvent.title, ignoreCase = true) &&
                        kotlin.math.abs(it.dateMillis - calEvent.startMillis) < 24 * 60 * 60 * 1000L
            }
            if (!duplicate) {
                val event = Event(
                    name = calEvent.title,
                    dateMillis = calEvent.startMillis,
                    colorIndex = DEFAULT_IMPORT_COLOR_INDEX,
                    emoji = "📅",
                    createdAt = System.currentTimeMillis()
                )
                EventStorage.addEvent(this, event)
                NotificationHelper.scheduleReminder(this, event, reminderDays)
                imported++
            } else {
                skipped++
            }
        }
        CountdownWidget.updateAllWidgets(this)
        val msg = when {
            skipped == 0 -> getString(R.string.events_imported, imported)
            imported == 0 -> getString(R.string.events_all_duplicates)
            else -> getString(R.string.events_imported_with_skipped, imported, skipped)
        }
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        if (imported > 0) finish()
    }
}
