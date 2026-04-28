package com.example.prog7313

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Expense::class, Budget::class, UserStats::class, Category::class, CategoryLimit::class],
    version = 10
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

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#C77921'")
            }
        }

        // Callback to pre-populate the Budget table on first creation
        private val prepopulateCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Use a coroutine to insert default budget after database is created
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        val budgetDao = database.budgetDao()
                        if (budgetDao.getBudget() == null) {
                            budgetDao.insertBudget(
                                Budget(
                                    monthlyGoal = 0.0,
                                    groceriesLimit = 0.0,
                                    transportLimit = 0.0,
                                    entertainmentLimit = 0.0,
                                    otherLimit = 0.0,
                                    billsLimit = 0.0
                                )
                            )
                        }
                    }
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_db"
                )
                    .addMigrations(MIGRATION_9_10)
                    .addCallback(prepopulateCallback)   // <-- ensures default budget exists
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}