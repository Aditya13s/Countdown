package com.countdown

import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
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
    private var selectedEmojiIndex: Int = 0
    private var selectedCategoryIndex: Int = -1  // -1 means "None"
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    /** Map from emoji string to its TextView, used for selection-ring management. */
    private val emojiViewMap = mutableMapOf<String, TextView>()

    /** Map from category string to its TextView, used for selection-ring management. */
    private val categoryViewMap = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_event)

        setupColorPicker()
        setupEmojiPicker()
        setupCategoryPicker()
        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveEvent() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ── Color picker ─────────────────────────────────────────────────────────

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

    // ── Emoji picker ─────────────────────────────────────────────────────────

    private fun setupEmojiPicker() {
        EVENT_EMOJIS.forEachIndexed { index, emoji ->
            val tv = TextView(this).apply {
                text = emoji
                textSize = 22f
                gravity = Gravity.CENTER
                val size = 44.dpToPx()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = 6.dpToPx()
                }
                setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                setOnClickListener { selectEmoji(index) }
            }
            emojiViewMap[emoji] = tv
            binding.llEmojiContainer.addView(tv)
        }
        selectEmoji(0)
    }

    private fun selectEmoji(index: Int) {
        if (EVENT_EMOJIS.isEmpty()) return
        val safeIndex = index.coerceIn(0, EVENT_EMOJIS.lastIndex)
        // Remove ring from previous selection
        emojiViewMap[EVENT_EMOJIS[selectedEmojiIndex]]?.background = null
        selectedEmojiIndex = safeIndex
        // Draw selection ring around new pick
        val ring = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.TRANSPARENT)
            setStroke(
                3.dpToPx(),
                ContextCompat.getColor(this@AddEventActivity, R.color.accent)
            )
        }
        emojiViewMap[EVENT_EMOJIS[safeIndex]]?.background = ring
    }

    // ── Category picker ───────────────────────────────────────────────────────

    private fun setupCategoryPicker() {
        EVENT_CATEGORIES.forEachIndexed { index, category ->
            val tv = TextView(this).apply {
                text = category
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(12.dpToPx(), 6.dpToPx(), 12.dpToPx(), 6.dpToPx())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 8.dpToPx() }
                setOnClickListener { selectCategory(index) }
            }
            setCategoryChipStyle(tv, selected = false)
            categoryViewMap[category] = tv
            binding.llCategoryContainer.addView(tv)
        }
    }

    private fun selectCategory(index: Int) {
        val safeIndex = index.coerceIn(0, EVENT_CATEGORIES.lastIndex)
        // Deselect previous
        if (selectedCategoryIndex >= 0) {
            categoryViewMap[EVENT_CATEGORIES[selectedCategoryIndex]]?.let {
                setCategoryChipStyle(it, selected = false)
            }
        }
        // Toggle off if same category tapped again
        if (selectedCategoryIndex == safeIndex) {
            selectedCategoryIndex = -1
            return
        }
        selectedCategoryIndex = safeIndex
        categoryViewMap[EVENT_CATEGORIES[safeIndex]]?.let { setCategoryChipStyle(it, selected = true) }
    }

    private fun setCategoryChipStyle(tv: TextView, selected: Boolean) {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20.dpToPx().toFloat()
            if (selected) {
                setColor(ContextCompat.getColor(this@AddEventActivity, R.color.accent))
            } else {
                setColor(ContextCompat.getColor(this@AddEventActivity, R.color.chip_bg))
            }
        }
        tv.background = bg
        tv.setTextColor(
            if (selected) ContextCompat.getColor(this, R.color.white)
            else ContextCompat.getColor(this, R.color.accent)
        )
    }

    // ── Date picker ──────────────────────────────────────────────────────────

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

    // ── Save ─────────────────────────────────────────────────────────────────

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
        val note = binding.etNote.text.toString().trim().ifEmpty { null }
        val category = if (selectedCategoryIndex >= 0) EVENT_CATEGORIES[selectedCategoryIndex] else null
        val event = Event(
            name = name,
            dateMillis = selectedDateMillis,
            colorIndex = selectedColorIndex,
            note = note,
            emoji = EVENT_EMOJIS[selectedEmojiIndex],
            createdAt = System.currentTimeMillis(),
            category = category,
            isPinned = binding.switchPin.isChecked
        )
        EventStorage.addEvent(this, event)
        CountdownWidget.updateAllWidgets(this)
        Toast.makeText(this, getString(R.string.event_added, name), Toast.LENGTH_SHORT).show()
        finish()
    }

    // ── Util ─────────────────────────────────────────────────────────────────

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density + 0.5f).toInt()
}

