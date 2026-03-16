package com.countdown

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.countdown.databinding.ItemEventBinding

class EventAdapter(
    private val events: MutableList<Event>,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val days = event.daysRemaining()
        holder.binding.tvEventName.text = event.name
        holder.binding.tvDaysCount.text = days.toString()
        holder.binding.tvDaysLabel.text = if (days == 1L) "day left" else "days left"
        holder.binding.btnDelete.setOnClickListener { onDelete(event) }
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
