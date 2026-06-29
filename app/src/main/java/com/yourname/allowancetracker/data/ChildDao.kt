package com.yourname.allowancetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY name ASC")
    fun getAllChildren(): Flow<List<Child>>

    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChild(childId: Int): Child?

    @Insert
    suspend fun insertChild(child: Child): Long

    @Update
    suspend fun updateChild(child: Child)

    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChild(childId: Int)
}