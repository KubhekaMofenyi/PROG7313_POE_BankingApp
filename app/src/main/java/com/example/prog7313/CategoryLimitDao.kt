package com.example.prog7313

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLimit(limit: CategoryLimit)

    @Query("SELECT * FROM category_limits")
    suspend fun getAllLimits(): List<CategoryLimit>

    @Query("SELECT * FROM category_limits WHERE categoryName = :category LIMIT 1")
    suspend fun getLimitForCategory(category: String): CategoryLimit?

    //ensure budget vs actual is up to date
    // New: delete limit when category is deleted
    @Query("DELETE FROM category_limits WHERE categoryName = :categoryName")
    suspend fun deleteLimitForCategory(categoryName: String)

    // New: Update limit's category name when the user renames a category
    @Query("UPDATE category_limits SET categoryName = :newName WHERE categoryName = :oldName")
    suspend fun updateCategoryNameInLimits(oldName: String, newName: String)
}