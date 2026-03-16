package com.countdown

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.countdown.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EventAdapter
    private val events = mutableListOf<Event>()

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

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun loadEvents() {
        val loaded = EventStorage.getEvents(this)
            .sortedBy { it.dateMillis }
        events.clear()
        events.addAll(loaded)
        adapter.notifyDataSetChanged()
        binding.tvEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun deleteEvent(event: Event) {
        EventStorage.deleteEvent(this, event.id)
        CountdownWidget.updateAllWidgets(this)
        loadEvents()
        Snackbar.make(binding.root, getString(R.string.event_deleted, event.name), Snackbar.LENGTH_SHORT).show()
    }
}
