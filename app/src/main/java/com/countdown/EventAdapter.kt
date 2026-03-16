package com.countdown

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.countdown.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(
    private val events: MutableList<Event>,
    private val onDelete: (Event) -> Unit,
    private val onEdit: (Event) -> Unit
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
        val context = holder.itemView.context

        // ── Emoji badge with accent-colour circle background ──────────────────
        val color = EVENT_COLORS.getOrElse(event.colorIndex) { EVENT_COLORS[0] }
        val badgeBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        holder.binding.tvEmoji.background = badgeBg
        holder.binding.tvEmoji.text = event.emoji ?: "🎯"

        // ── Pin indicator ─────────────────────────────────────────────────────
        holder.binding.tvPin.visibility = if (event.isPinned) View.VISIBLE else View.GONE

        // ── Text fields ───────────────────────────────────────────────────────
        holder.binding.tvEventName.text = event.name
        holder.binding.tvEventDate.text = dateFormat.format(Date(event.dateMillis))

        // ── Category chip ─────────────────────────────────────────────────────
        val category = event.category?.trim()
        if (!category.isNullOrEmpty()) {
            holder.binding.tvCategory.text = category
            holder.binding.tvCategory.visibility = View.VISIBLE
        } else {
            holder.binding.tvCategory.visibility = View.GONE
        }

        val note = event.note?.trim()
        if (!note.isNullOrEmpty()) {
            holder.binding.tvNote.text = note
            holder.binding.tvNote.visibility = View.VISIBLE
        } else {
            holder.binding.tvNote.visibility = View.GONE
        }

        // ── Progress bar ──────────────────────────────────────────────────────
        val pct = event.progressPercent()
        holder.binding.progressIndicator.progress = pct

        // ── Live countdown ticker ─────────────────────────────────────────────
        fun tick() {
            val tc = event.timeComponents()
            if (tc.isZero) {
                holder.binding.tvCountdown.text = context.getString(R.string.event_passed)
                holder.binding.tvMilestone.visibility = View.GONE
                return
            }
            holder.binding.tvCountdown.text = when {
                tc.days > 0 -> context.getString(
                    R.string.countdown_dhms, tc.days, tc.hours, tc.minutes, tc.seconds
                )
                tc.hours > 0 -> context.getString(
                    R.string.countdown_hms, tc.hours, tc.minutes, tc.seconds
                )
                else -> context.getString(
                    R.string.countdown_ms, tc.minutes, tc.seconds
                )
            }

            // ── Milestone message ─────────────────────────────────────────────
            val days = tc.days
            val (milestoneText, milestoneColorRes) = when {
                days == 0L -> context.getString(R.string.milestone_today) to R.color.milestone_urgent
                days <= 3 -> context.getString(R.string.milestone_days_3) to R.color.milestone_urgent
                days <= 7 -> context.getString(R.string.milestone_days_7) to R.color.milestone_soon
                days <= 30 -> context.getString(R.string.milestone_days_30) to R.color.milestone_near
                days <= 100 -> context.getString(R.string.milestone_days_100) to R.color.milestone_normal
                else -> "" to R.color.milestone_normal
            }
            if (milestoneText.isNotEmpty()) {
                holder.binding.tvMilestone.text = milestoneText
                holder.binding.tvMilestone.setTextColor(ContextCompat.getColor(context, milestoneColorRes))
                holder.binding.tvMilestone.visibility = View.VISIBLE
            } else {
                holder.binding.tvMilestone.visibility = View.GONE
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

        // ── Buttons ───────────────────────────────────────────────────────────
        holder.binding.btnDelete.setOnClickListener { onDelete(event) }
        holder.binding.btnEdit.setOnClickListener { onEdit(event) }

        holder.binding.btnShare.setOnClickListener {
            val tc = event.timeComponents()
            val countdownText = when {
                tc.isZero -> context.getString(R.string.event_passed)
                tc.days > 0 -> context.getString(
                    R.string.countdown_dhms, tc.days, tc.hours, tc.minutes, tc.seconds
                )
                tc.hours > 0 -> context.getString(
                    R.string.countdown_hms, tc.hours, tc.minutes, tc.seconds
                )
                else -> context.getString(
                    R.string.countdown_ms, tc.minutes, tc.seconds
                )
            }
            val shareText = "${event.emoji ?: "🎯"} ${event.name}\n$countdownText"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
        }
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
