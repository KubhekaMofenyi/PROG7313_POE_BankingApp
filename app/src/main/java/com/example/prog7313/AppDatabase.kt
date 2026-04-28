package com.example.prog7313

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
//
@Database(
    entities = [User::class, Expense::class, Budget::class, UserStats::class, Category::class, CategoryLimit::class],
    version = 10   // increased from 9 to 10
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryLimitDao(): CategoryLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 9 to 10 – adds 'color' column to categorise table
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add color column with default value. Existing rows will get '#C77921'
                database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#C77921'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_db"
                )
                    .addMigrations(MIGRATION_9_10)   // use the migration to preserve data
                    .fallbackToDestructiveMigration() // safety net (won't run if migration works)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}