package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val lastActiveDate: String,
    val streak: Int
)