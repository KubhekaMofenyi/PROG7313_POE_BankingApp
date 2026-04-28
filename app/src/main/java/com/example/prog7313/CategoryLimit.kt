package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_limits")
data class CategoryLimit(
    @PrimaryKey val categoryName: String,
    val limitAmount: Double
)