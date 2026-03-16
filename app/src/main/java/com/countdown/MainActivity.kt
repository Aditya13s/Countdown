package com.countdown

import android.content.Intent
import android.os.Bundle
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
    private val events = mutableListOf<Event>()

    private enum class SortMode { DATE, NAME }
    private var sortMode = SortMode.DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = EventAdapter(events) { event -> deleteEvent(event) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                sortMode = if (sortMode == SortMode.DATE) SortMode.NAME else SortMode.DATE
                val msg = if (sortMode == SortMode.DATE)
                    getString(R.string.sort_by_date)
                else
                    getString(R.string.sort_by_name)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                loadEvents()
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
        val loaded = EventStorage.getEvents(this).let { list ->
            val sorted = when (sortMode) {
                SortMode.DATE -> list.sortedBy { it.dateMillis }
                SortMode.NAME -> list.sortedBy { it.name.lowercase() }
            }
            // Pinned events always appear first
            sorted.sortedByDescending { it.isPinned }
        }
        events.clear()
        events.addAll(loaded)
        adapter.notifyDataSetChanged()
        val isEmpty = events.isEmpty()
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun deleteEvent(event: Event) {
        EventStorage.deleteEvent(this, event.id)
        CountdownWidget.updateAllWidgets(this)
        loadEvents()
        Snackbar.make(binding.root, getString(R.string.event_deleted, event.name), Snackbar.LENGTH_SHORT).show()
    }
}
