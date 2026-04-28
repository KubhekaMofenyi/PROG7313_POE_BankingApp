package com.example.prog7313

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalSpent(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    suspend fun getSpentByCategory(category: String): Double?

    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category")
    suspend fun getCategoryTotals(): List<CategoryTotal>

    @Query(""" SELECT date, SUM(amount) as total FROM expenses GROUP BY date ORDER BY date ASC""")
    suspend fun getDailyTotals(): List<DailyTotal>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)

    @Update
    suspend fun updateExpense(expense: Expense)

    //adding feature where if category is deleted assigned to uncategorized

    @Query("UPDATE expenses SET category = :newCategory WHERE category = :oldCategory")
    suspend fun reassignCategory(oldCategory: String, newCategory: String)

    // adding filter query
    @Query("SELECT * FROM expenses WHERE " +
            "(:keyword IS NULL OR category LIKE '%' || :keyword || '%' OR notes LIKE '%' || :keyword || '%' OR date LIKE '%' || :keyword || '%') AND " +
            "(:category IS NULL OR category = :category) AND " +
            "(:startDate IS NULL OR date >= :startDate) AND " +
            "(:endDate IS NULL OR date <= :endDate) " +
            "ORDER BY date DESC")
    suspend fun getFilteredExpenses(
        keyword: String?,
        category: String?,
        startDate: String?,
        endDate: String?
    ): List<Expense>

}