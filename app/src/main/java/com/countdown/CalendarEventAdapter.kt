package com.countdown

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.countdown.databinding.ItemCalendarEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarEventAdapter(
    private val events: List<CalendarEvent>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy · HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemCalendarEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemCalendarEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.binding.tvTitle.text = event.title
        holder.binding.tvDate.text = dateFormat.format(Date(event.startMillis))
        holder.binding.cbSelect.isChecked = event.isSelected

        val toggle = {
            event.isSelected = !event.isSelected
            holder.binding.cbSelect.isChecked = event.isSelected
            onSelectionChanged()
        }
        holder.binding.root.setOnClickListener { toggle() }
        holder.binding.cbSelect.setOnClickListener {
            event.isSelected = holder.binding.cbSelect.isChecked
            onSelectionChanged()
        }
    }

    override fun getItemCount() = events.size
}
