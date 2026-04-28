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
}