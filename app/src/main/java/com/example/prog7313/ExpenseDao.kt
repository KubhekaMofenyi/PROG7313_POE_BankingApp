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
}