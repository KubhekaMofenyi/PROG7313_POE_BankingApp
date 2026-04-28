package com.example.prog7313

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// adding update, delete, duplicate checker functionality
@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Query("SELECT DISTINCT name FROM categories ORDER BY name ASC")
    suspend fun getCategoryNames(): List<String>

    // Case‑insensitive duplicate check
    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByNameIgnoreCase(name: String): Int
}