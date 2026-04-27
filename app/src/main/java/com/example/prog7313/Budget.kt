package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    @PrimaryKey val id: Int = 1, // only one budget per user for now
    val monthlyGoal: Double
)