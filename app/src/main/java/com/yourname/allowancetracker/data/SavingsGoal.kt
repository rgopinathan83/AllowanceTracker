package com.yourname.allowancetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val childId: Int,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val icon: String = "🎯",
    val createdDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null
) {
    fun getProgress(): Float {
        return if (targetAmount > 0) (savedAmount / targetAmount).toFloat() else 0f
    }

    fun getRemainingAmount(): Double {
        return targetAmount - savedAmount
    }

    fun isFullyFunded(): Boolean {
        return savedAmount >= targetAmount
    }

    fun getFormattedProgress(): String {
        return "${(getProgress() * 100).toInt()}%"
    }
}

@Entity(tableName = "goal_transactions")
data class GoalTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val goalId: Int,
    val childId: Int,
    val amount: Double,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)