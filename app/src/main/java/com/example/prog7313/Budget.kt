package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    @PrimaryKey val id: Int = 1,
    val monthlyGoal: Double,
    val groceriesLimit: Double,
    val transportLimit: Double,
    val entertainmentLimit: Double,
    val otherLimit: Double,
    val billsLimit: Double
)