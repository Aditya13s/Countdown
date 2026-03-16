package com.countdown

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.countdown.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EventAdapter
    private val allEvents = mutableListOf<Event>()
    private val displayedEvents = mutableListOf<Event>()
    private var searchQuery: String = ""

    private enum class SortMode { DATE, NAME }
    private var sortMode = SortMode.DATE

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Restore persisted sort mode
        sortMode = if (EventStorage.getSortMode(this) == "name") SortMode.NAME else SortMode.DATE

        adapter = EventAdapter(displayedEvents,
            onDelete = { event -> deleteEvent(event) },
            onEdit = { event -> editEvent(event) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilterAndDisplay()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                sortMode = if (sortMode == SortMode.DATE) SortMode.NAME else SortMode.DATE
                EventStorage.setSortMode(this, if (sortMode == SortMode.DATE) "date" else "name")
                val msg = if (sortMode == SortMode.DATE)
                    getString(R.string.sort_by_date)
                else
                    getString(R.string.sort_by_name)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                applyFilterAndDisplay()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun loadEvents() {
        val loaded = EventStorage.getEvents(this)
        allEvents.clear()
        allEvents.addAll(loaded)
        applyFilterAndDisplay()
    }

    private fun applyFilterAndDisplay() {
        val filtered = if (searchQuery.isBlank()) {
            allEvents.toList()
        } else {
            val q = searchQuery.trim().lowercase()
            allEvents.filter {
                it.name.lowercase().contains(q) ||
                        it.category?.lowercase()?.contains(q) == true ||
                        it.note?.lowercase()?.contains(q) == true
            }
        }

        val sorted = when (sortMode) {
            SortMode.DATE -> filtered.sortedBy { it.dateMillis }
            SortMode.NAME -> filtered.sortedBy { it.name.lowercase() }
        }.sortedByDescending { it.isPinned }

        displayedEvents.clear()
        displayedEvents.addAll(sorted)
        adapter.notifyDataSetChanged()

        val isEmpty = displayedEvents.isEmpty()
        val noResults = isEmpty && searchQuery.isNotBlank()
        binding.tvEmpty.visibility = if (isEmpty && searchQuery.isBlank()) View.VISIBLE else View.GONE
        binding.tvNoResults.visibility = if (noResults) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun deleteEvent(event: Event) {
        NotificationHelper.cancelReminder(this, event.id)
        EventStorage.deleteEvent(this, event.id)
        CountdownWidget.updateAllWidgets(this)
        loadEvents()
        Snackbar.make(binding.root, getString(R.string.event_deleted, event.name), Snackbar.LENGTH_SHORT).show()
    }

    private fun editEvent(event: Event) {
        val intent = Intent(this, AddEventActivity::class.java).apply {
            putExtra(EXTRA_EVENT_ID, event.id)
        }
        startActivity(intent)
    }
}
