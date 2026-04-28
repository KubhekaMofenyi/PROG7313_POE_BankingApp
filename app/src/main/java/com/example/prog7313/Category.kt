package com.example.prog7313

import androidx.room.Entity
import androidx.room.PrimaryKey
// adding the category feature with colour for categories // left out icons
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String = "#C77921"   // default orange (used for existing categories)
)