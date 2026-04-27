package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class GamificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)

        val tvStreak = findViewById<TextView>(R.id.tvCurrentStreak)
        val tvLevel = findViewById<TextView>(R.id.tvBudgetLevel)
        val tvBadge1 = findViewById<TextView>(R.id.tvBadge1)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        btnHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnFinance.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        val db = AppDatabase.getDatabase(this)
        val statsDao = db.userStatsDao()
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        lifecycleScope.launch {
            val stats = statsDao.getStats()
            val budget = budgetDao.getBudget()

            val totalBalance = budget?.monthlyGoal ?: 10000.0
            val totalSpent = expenseDao.getTotalSpent() ?: 0.0

            val streak = stats?.streak ?: 0

            val level = when {
                totalSpent < totalBalance * 0.5 -> "Gold"
                totalSpent < totalBalance -> "Silver"
                else -> "Bronze"
            }

            val badge = when {
                streak >= 7 -> "Consistency Badge"
                totalSpent < totalBalance * 0.7 -> "Smart Spender"
                else -> "Getting Started"
            }

            tvStreak.text = "Streak: $streak days"
            tvLevel.text = level
            tvBadge1.text = "Badge: $badge"
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnFinance.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}