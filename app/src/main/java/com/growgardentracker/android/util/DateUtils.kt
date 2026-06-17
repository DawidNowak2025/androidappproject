package com.growgardentracker.android.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun today(): String = formatter.format(Date())

    fun nextWateringDate(lastWateredDate: String, frequencyDays: Int): String {
        val calendar = Calendar.getInstance()
        val parsed = runCatching { formatter.parse(lastWateredDate) }.getOrNull()
        if (parsed != null) calendar.time = parsed
        calendar.add(Calendar.DAY_OF_YEAR, frequencyDays.coerceAtLeast(1))
        return formatter.format(calendar.time)
    }

    fun wateringStatus(nextWateringDate: String): String {
        val todayDate = runCatching { formatter.parse(today()) }.getOrNull() ?: return "Unknown"
        val nextDate = runCatching { formatter.parse(nextWateringDate) }.getOrNull() ?: return "Unknown"
        return when {
            nextDate.before(todayDate) -> "Overdue"
            formatter.format(nextDate) == formatter.format(todayDate) -> "Water Today"
            else -> "Upcoming"
        }
    }

    fun currentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }
}
