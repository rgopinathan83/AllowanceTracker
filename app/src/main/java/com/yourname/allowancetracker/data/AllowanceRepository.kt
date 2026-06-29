package com.yourname.allowancetracker.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.min

class AllowanceRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val childDao = database.childDao()
    private val transactionDao = database.transactionDao()
    private val recurringDao = database.recurringAllowanceDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val goalTransactionDao = database.goalTransactionDao()

    // ============================================
    // CHILDREN
    // ============================================

    fun getAllChildren(): Flow<List<Child>> = childDao.getAllChildren()

    suspend fun addChild(name: String): Long {
        val child = Child(name = name, balance = 0.0)
        return childDao.insertChild(child)
    }

    suspend fun deleteChild(childId: Int) {
        transactionDao.deleteTransactionsForChild(childId)
        recurringDao.deleteAllowancesForChild(childId)
        savingsGoalDao.deleteGoalsForChild(childId)
        goalTransactionDao.deleteTransactionsForChild(childId)
        childDao.deleteChild(childId)
    }

    // ============================================
    // TRANSACTIONS
    // ============================================

    fun getTransactionsForChild(childId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsForChild(childId)

    suspend fun addTransaction(childId: Int, amount: Double, description: String) {
        val transaction = Transaction(
            childId = childId,
            amount = amount,
            description = description
        )

        transactionDao.insertTransaction(transaction)
        updateChildBalance(childId)
    }

    suspend fun updateTransaction(transaction: Transaction, newAmount: Double, newDescription: String) {
        val updatedTransaction = transaction.copy(
            amount = newAmount,
            description = newDescription
        )
        transactionDao.updateTransaction(updatedTransaction)
        updateChildBalance(transaction.childId)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        updateChildBalance(transaction.childId)
    }

    private suspend fun updateChildBalance(childId: Int) {
        val transactions = transactionDao.getTransactionsForChild(childId).first()
        val newBalance = transactions.sumOf { it.amount }

        val child = childDao.getChild(childId)
        child?.let {
            childDao.updateChild(it.copy(balance = newBalance))
        }
    }

    // ============================================
    // RECURRING ALLOWANCE MANAGEMENT
    // ============================================

    suspend fun addRecurringAllowance(childId: Int, amount: Double, frequency: String, day: Int) {
        val allowance = RecurringAllowance(
            childId = childId,
            amount = amount,
            frequency = frequency,
            day = day,
            isActive = true
        )
        recurringDao.insertAllowance(allowance)
    }

    fun getRecurringAllowance(childId: Int): Flow<List<RecurringAllowance>> {
        return recurringDao.getAllowanceForChild(childId)
    }

    suspend fun toggleRecurringAllowance(allowanceId: Int, isActive: Boolean) {
        recurringDao.updateAllowanceStatus(allowanceId, isActive)
    }

    suspend fun updateRecurringAllowance(allowance: RecurringAllowance) {
        recurringDao.updateAllowance(allowance)
    }

    suspend fun deleteRecurringAllowance(allowanceId: Int) {
        recurringDao.deleteAllowance(allowanceId)
    }

    suspend fun applyScheduledAllowances() {
        val activeAllowances = recurringDao.getActiveAllowances().first()

        activeAllowances.forEach { allowance ->
            if (allowance.shouldRunToday()) {
                val existingToday = transactionDao.getTransactionsForChild(allowance.childId)
                    .first()
                    .filter {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it.timestamp
                        cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    }
                    .any { it.description.contains("Allowance") }

                if (!existingToday) {
                    addTransaction(
                        childId = allowance.childId,
                        amount = allowance.amount,
                        description = "${allowance.frequency} Allowance"
                    )
                }
            }
        }
    }

    // ============================================
    // GOALS
    // ============================================

    fun getGoalsForChild(childId: Int): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getGoalsForChild(childId)
    }

    fun getGoalTransactions(goalId: Int): Flow<List<GoalTransaction>> {
        return goalTransactionDao.getTransactionsForGoal(goalId)
    }

    suspend fun createGoal(childId: Int, name: String, targetAmount: Double, icon: String) {
        val goal = SavingsGoal(
            childId = childId,
            name = name,
            targetAmount = targetAmount,
            icon = icon
        )
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun allocateToGoal(childId: Int, goalId: Int, amount: Double, note: String) {
        savingsGoalDao.addToGoal(goalId, amount)

        val goalTransaction = GoalTransaction(
            goalId = goalId,
            childId = childId,
            amount = amount,
            note = note
        )
        goalTransactionDao.insertGoalTransaction(goalTransaction)

        addTransaction(childId, -amount, "Savings: $note")

        val goal = savingsGoalDao.getGoalById(goalId)
        goal?.let {
            if (it.savedAmount >= it.targetAmount) {
                savingsGoalDao.updateGoal(
                    it.copy(
                        isCompleted = true,
                        completedDate = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun withdrawFromGoal(childId: Int, goal: SavingsGoal, amount: Double, note: String) {
        val withdrawAmount = minOf(amount, goal.savedAmount)

        savingsGoalDao.addToGoal(goal.id, -withdrawAmount)

        addTransaction(childId, withdrawAmount, "Withdraw from goal: $note")

        if (goal.isCompleted) {
            savingsGoalDao.updateGoal(
                goal.copy(
                    isCompleted = false,
                    completedDate = null
                )
            )
        }
    }
}