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
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val isActive: Boolean = true
)

// Helper to check if today matches the schedule
fun RecurringAllowance.shouldRunToday(): Boolean {
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    // Convert Calendar's Sunday=1 to our Monday=1 system
    val adjustedToday = if (today == Calendar.SUNDAY) 7 else today - 1
    return adjustedToday == dayOfWeek
}