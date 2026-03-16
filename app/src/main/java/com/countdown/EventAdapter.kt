package com.countdown

import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.countdown.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val events: MutableList<Event>,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class EventViewHolder(val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val handler = Handler(Looper.getMainLooper())
        var timerRunnable: Runnable? = null

        fun stopTimer() {
            timerRunnable?.let { handler.removeCallbacks(it) }
            timerRunnable = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.stopTimer()
        val event = events[position]

        // Accent colour stripe
        val color = EVENT_COLORS.getOrElse(event.colorIndex) { EVENT_COLORS[0] }
        val stripeDrawable = GradientDrawable()
        stripeDrawable.setColor(color)
        holder.binding.colorStripe.background = stripeDrawable

        holder.binding.tvEventName.text = event.name
        holder.binding.tvEventDate.text = dateFormat.format(Date(event.dateMillis))

        // Live countdown ticker
        fun tick() {
            val tc = event.timeComponents()
            if (tc.isZero) {
                holder.binding.tvCountdown.text = holder.itemView.context.getString(R.string.event_passed)
                return
            }
            holder.binding.tvCountdown.text = when {
                tc.days > 0 -> holder.itemView.context.getString(
                    R.string.countdown_dhms, tc.days, tc.hours, tc.minutes, tc.seconds
                )
                tc.hours > 0 -> holder.itemView.context.getString(
                    R.string.countdown_hms, tc.hours, tc.minutes, tc.seconds
                )
                else -> holder.itemView.context.getString(
                    R.string.countdown_ms, tc.minutes, tc.seconds
                )
            }
        }

        tick()

        val runnable = object : Runnable {
            override fun run() {
                tick()
                holder.handler.postDelayed(this, 1000)
            }
        }
        holder.timerRunnable = runnable
        holder.handler.postDelayed(runnable, 1000)

        holder.binding.btnDelete.setOnClickListener { onDelete(event) }
    }

    override fun onViewRecycled(holder: EventViewHolder) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    override fun onViewDetachedFromWindow(holder: EventViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.stopTimer()
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
