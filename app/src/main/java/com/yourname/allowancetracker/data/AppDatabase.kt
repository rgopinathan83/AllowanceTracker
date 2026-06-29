package com.yourname.allowancetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Child::class,
        Transaction::class,
        RecurringAllowance::class,
        SavingsGoal::class,
        GoalTransaction::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringAllowanceDao(): RecurringAllowanceDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun goalTransactionDao(): GoalTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "allowance_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}