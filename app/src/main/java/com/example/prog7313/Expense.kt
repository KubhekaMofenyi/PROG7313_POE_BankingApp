package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val category: String,
    val notes: String,
    val hasReceipt: Boolean = false,
    val receiptUri: String? = null
)