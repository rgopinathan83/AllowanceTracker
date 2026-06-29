package com.yourname.allowancetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringAllowanceDao {
    @Query("SELECT * FROM recurring_allowances WHERE childId = :childId ORDER BY isActive DESC, id ASC")
    fun getAllowanceForChild(childId: Int): Flow<List<RecurringAllowance>>

    @Query("SELECT * FROM recurring_allowances WHERE isActive = 1")
    fun getActiveAllowances(): Flow<List<RecurringAllowance>>

    @Insert
    suspend fun insertAllowance(allowance: RecurringAllowance): Long

    @Update
    suspend fun updateAllowance(allowance: RecurringAllowance)

    @Query("UPDATE recurring_allowances SET isActive = :isActive WHERE id = :allowanceId")
    suspend fun updateAllowanceStatus(allowanceId: Int, isActive: Boolean)

    @Query("DELETE FROM recurring_allowances WHERE id = :allowanceId")
    suspend fun deleteAllowance(allowanceId: Int)

    @Query("DELETE FROM recurring_allowances WHERE childId = :childId")
    suspend fun deleteAllowancesForChild(childId: Int)
}