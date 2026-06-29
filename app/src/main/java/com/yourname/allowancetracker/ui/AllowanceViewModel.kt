package com.yourname.allowancetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.allowancetracker.data.AllowanceRepository
import com.yourname.allowancetracker.data.SavingsGoal
import com.yourname.allowancetracker.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllowanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AllowanceRepository(application)

    // UI State
    private val _uiState = MutableStateFlow(AllowanceUiState())
    val uiState: StateFlow<AllowanceUiState> = _uiState.asStateFlow()

    // Selected child for detailed view
    private val _selectedChildId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            // Apply scheduled allowances when app starts
            repository.applyScheduledAllowances()
        }

        // Observe children and combine with transactions
        viewModelScope.launch {
            repository.getAllChildren().collect { children ->
                _uiState.value = _uiState.value.copy(children = children)
            }
        }
    }

    // ============================================
    // CHILD MANAGEMENT
    // ============================================

    fun selectChild(childId: Int) {
        _selectedChildId.value = childId
        viewModelScope.launch {
            repository.getTransactionsForChild(childId).collect { transactions ->
                val child = _uiState.value.children.find { it.id == childId }
                _uiState.value = _uiState.value.copy(
                    selectedChild = child,
                    transactions = transactions
                )
            }
        }
    }

    fun addChild(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.addChild(name)
            }
        }
    }

    fun deleteChild(childId: Int) {
        viewModelScope.launch {
            repository.deleteChild(childId)
            if (_selectedChildId.value == childId) {
                _selectedChildId.value = null
                _uiState.value = _uiState.value.copy(
                    selectedChild = null,
                    transactions = emptyList()
                )
            }
        }
    }

    // ============================================
    // TRANSACTION MANAGEMENT
    // ============================================

    fun addTransaction(childId: Int, amount: Double, description: String) {
        if (description.isNotBlank() && amount != 0.0) {
            viewModelScope.launch {
                repository.addTransaction(childId, amount, description)
                // Refresh selected child data
                _selectedChildId.value?.let { selectChild(it) }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            // Refresh selected child data
            _selectedChildId.value?.let { selectChild(it) }
        }
    }

    fun updateTransaction(transaction: Transaction, newAmount: Double, newDescription: String) {
        if (newDescription.isNotBlank() && newAmount != 0.0) {
            viewModelScope.launch {
                repository.updateTransaction(transaction, newAmount, newDescription)
                // Refresh selected child data
                _selectedChildId.value?.let { selectChild(it) }
            }
        }
    }

    // ============================================
    // RECURRING ALLOWANCE MANAGEMENT
    // ============================================

    // ✅ FIXED: Match the signature expected by the dialog
    fun addRecurringAllowance(childId: Int, amount: Double, frequency: String, day: Int) {
        if (amount > 0) {
            viewModelScope.launch {
                repository.addRecurringAllowance(childId, amount, frequency, day)
            }
        }
    }

    // ============================================
    // GOALS MANAGEMENT
    // ============================================

    fun getGoalsForChild(childId: Int) = repository.getGoalsForChild(childId)

    fun getGoalTransactions(goalId: Int) = repository.getGoalTransactions(goalId)

    fun createGoal(childId: Int, name: String, targetAmount: Double, icon: String) {
        if (name.isNotBlank() && targetAmount > 0) {
            viewModelScope.launch {
                repository.createGoal(childId, name, targetAmount, icon)
            }
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun allocateToGoal(childId: Int, goalId: Int, amount: Double, note: String) {
        if (amount > 0) {
            viewModelScope.launch {
                repository.allocateToGoal(childId, goalId, amount, note)
                // Refresh data
                _selectedChildId.value?.let { selectChild(it) }
            }
        }
    }

    fun withdrawFromGoal(childId: Int, goal: SavingsGoal, amount: Double, note: String) {
        if (amount > 0) {
            viewModelScope.launch {
                repository.withdrawFromGoal(childId, goal, amount, note)
                // Refresh data
                _selectedChildId.value?.let { selectChild(it) }
            }
        }
    }
}

data class AllowanceUiState(
    val children: List<com.yourname.allowancetracker.data.Child> = emptyList(),
    val selectedChild: com.yourname.allowancetracker.data.Child? = null,
    val transactions: List<com.yourname.allowancetracker.data.Transaction> = emptyList(),
    val isLoading: Boolean = false
)