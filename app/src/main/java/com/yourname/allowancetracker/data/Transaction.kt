package com.yourname.allowancetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val childId: Int,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Helper function to format date
fun Transaction.getFormattedDate(): String {
    val date = Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}