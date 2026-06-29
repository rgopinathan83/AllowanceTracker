package com.yourname.allowancetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE childId = :childId ORDER BY isCompleted ASC, createdDate DESC")
    fun getGoalsForChild(childId: Int): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Int): SavingsGoal?

    @Insert
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("UPDATE savings_goals SET savedAmount = savedAmount + :amount WHERE id = :goalId")
    suspend fun addToGoal(goalId: Int, amount: Double)

    @Query("DELETE FROM savings_goals WHERE childId = :childId")
    suspend fun deleteGoalsForChild(childId: Int)
}

@Dao
interface GoalTransactionDao {
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getTransactionsForGoal(goalId: Int): Flow<List<GoalTransaction>>

    @Query("SELECT * FROM goal_transactions WHERE childId = :childId ORDER BY timestamp DESC")
    fun getAllGoalTransactions(childId: Int): Flow<List<GoalTransaction>>

    @Insert
    suspend fun insertGoalTransaction(transaction: GoalTransaction)

    @Query("DELETE FROM goal_transactions WHERE childId = :childId")
    suspend fun deleteTransactionsForChild(childId: Int)
}