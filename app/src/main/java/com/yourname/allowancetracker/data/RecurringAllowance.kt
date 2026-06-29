package com.yourname.allowancetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "recurring_allowances")
data class RecurringAllowance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val childId: Int,
    val amount: Double,
    val frequency: String = "Weekly", // "Weekly" or "Monthly"
    val day: Int = 1, // 1-7 for weekly (Mon-Sun), 1-28 for monthly
    val isActive: Boolean = true
) {
    fun shouldRunToday(): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        return when (frequency) {
            "Weekly" -> {
                val adjustedToday = if (today == Calendar.SUNDAY) 7 else today - 1
                adjustedToday == day
            }
            "Monthly" -> {
                dayOfMonth == day
            }
            else -> false
        }
    }

    fun getDisplayText(): String {
        val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        return when (frequency) {
            "Weekly" -> "Every ${weekDays[day - 1]}"
            "Monthly" -> {
                val suffix = when (day) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
                "Every $day$suffix of the month"
            }
            else -> "Unknown"
        }
    }
}