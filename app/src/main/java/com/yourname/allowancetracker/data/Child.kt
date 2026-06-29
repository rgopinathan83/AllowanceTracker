package com.yourname.allowancetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class Child(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Double = 0.0
)