package com.example.prog7313

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("""
        SELECT * FROM categories
        WHERE id IN (
            SELECT MIN(id)
            FROM categories
            GROUP BY LOWER(name)
        )
        ORDER BY name ASC
    """)
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Query("""
        SELECT name FROM categories
        WHERE id IN (
            SELECT MIN(id)
            FROM categories
            GROUP BY LOWER(name)
        )
        ORDER BY name ASC
    """)
    suspend fun getCategoryNames(): List<String>

    @Query("SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByNameIgnoreCase(name: String): Int
}