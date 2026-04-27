package com.example.prog7313

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private fun loadData() {
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val tvAvailableBalance = findViewById<TextView>(R.id.tvAvailableBalance)
        val tvBudgetInfo = findViewById<TextView>(R.id.tvBudgetInfo)
        val tvBudgetStatus = findViewById<TextView>(R.id.tvBudgetStatus)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val tvSafeSpend = findViewById<TextView>(R.id.tvSafeSpend)
        val tvOverspendingMessage = findViewById<TextView>(R.id.tvOverspendingMessage)
        val tvInsightMessage = findViewById<TextView>(R.id.tvInsightMessage)
        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)

        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        lifecycleScope.launch {
            val budget = budgetDao.getBudget()
            //not too sure if i need this line and cant remember where it went :D
            val totalBalance = budget?.monthlyGoal ?: 0.0
            val totalSpent = expenseDao.getTotalSpent() ?: 0.0

            //consistency with finance page
            val categoryWarnings = mutableListOf<String>()

            if (budget != null) {
                val categories = listOf(
                    "Groceries" to budget.groceriesLimit,
                    "Transport" to budget.transportLimit,
                    "Bills" to budget.billsLimit,
                    "Entertainment" to budget.entertainmentLimit,
                    "Other" to budget.otherLimit
                )

                categories.forEach { (category, limit) ->
                    val spent = expenseDao.getSpentByCategory(category) ?: 0.0

                    if (limit > 0 && spent > limit) {
                        categoryWarnings.add(
                            "$category is over budget by R%,.0f.".format(spent - limit)
                        )
                    } else if (limit > 0 && spent >= limit * 0.8) {
                        categoryWarnings.add(
                            "$category is close to its limit. R%,.0f remaining.".format(limit - spent)
                        )
                    }
                }
            }

            val remaining = totalBalance - totalSpent
            val safeSpendToday = kotlin.math.max(remaining / 22, 0.0)

            tvTotalBalance.text = "R%,.0f".format(totalBalance)
            tvAvailableBalance.text = "R%,.0f".format(remaining)
            tvBudgetInfo.text = "Spent R%,.0f of R%,.0f".format(totalSpent, totalBalance)
            tvRemaining.text = "R%,.0f".format(remaining)
            tvSafeSpend.text = "R%,.0f".format(safeSpendToday)

            progressBudget.max = totalBalance.toInt()
            progressBudget.progress = totalSpent.toInt()

            when {
                totalSpent > totalBalance -> {
                    tvBudgetStatus.text = "Warning. You have exceeded your budget."
                    tvOverspendingMessage.text = "You are over budget by R%,.0f.".format(totalSpent - totalBalance)
                }
                totalSpent >= totalBalance * 0.8 -> {
                    tvBudgetStatus.text = "Caution. You are close to your budget limit."
                    tvOverspendingMessage.text = "You have R%,.0f remaining this month.".format(remaining)
                }
                else -> {
                    tvBudgetStatus.text = "Excellent. You are well within budget."
                    tvOverspendingMessage.text = "You have R%,.0f available to spend.".format(remaining)
                }
            }

            val cardOverspending = findViewById<LinearLayout>(R.id.cardOverspending)

            if (categoryWarnings.isNotEmpty()) {
                cardOverspending.visibility = View.VISIBLE
                tvOverspendingMessage.text = categoryWarnings.joinToString("\n")
            } else {
                cardOverspending.visibility = View.VISIBLE
                tvOverspendingMessage.text = "No category overspending detected."
            }

            val categoryTotals = expenseDao.getCategoryTotals()
            val highestCategory = categoryTotals.maxByOrNull { it.total }

            val insight = when {
                categoryTotals.isEmpty() -> {
                    "No spending insights yet. Add expenses to get started."
                }

                totalSpent > totalBalance -> {
                    "⚠ You have exceeded your monthly budget."
                }

                highestCategory != null && highestCategory.total >= totalSpent * 0.4 -> {
                    "You're spending heavily on ${highestCategory.category} this month."
                }

                totalSpent < totalBalance * 0.5 -> {
                    "Great job! You're well within your budget."
                }

                else -> {
                    "Your spending is balanced across categories."
                }
            }
            tvInsightMessage.text = insight

            //gamification
            val statsDao = db.userStatsDao()

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val stats = statsDao.getStats()

            val newStreak = when {
                stats == null -> 1
                stats.lastActiveDate == today -> stats.streak
                else -> stats.streak + 1
            }

            statsDao.saveStats(
                UserStats(
                    lastActiveDate = today,
                    streak = newStreak
                )
            )
            //level
            val level = when {
                totalSpent < totalBalance * 0.5 -> "Gold"
                totalSpent < totalBalance -> "Silver"
                else -> "Bronze"
            }
            //badge
            val badge = when {
                newStreak >= 7 -> "Consistency Badge"
                totalSpent < totalBalance * 0.7 -> "Smart Spender"
                else -> "Getting Started"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnFinanceArrow = findViewById<ImageButton>(R.id.btnFinanceArrow)
        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val tvAvailableBalance = findViewById<TextView>(R.id.tvAvailableBalance)
        val tvBudgetInfo = findViewById<TextView>(R.id.tvBudgetInfo)
        val tvBudgetStatus = findViewById<TextView>(R.id.tvBudgetStatus)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val tvSafeSpend = findViewById<TextView>(R.id.tvSafeSpend)
        val tvRecentTip = findViewById<TextView>(R.id.tvRecentTip)
        val tvOverspendingMessage = findViewById<TextView>(R.id.tvOverspendingMessage)
        val tvAchievementSummary = findViewById<TextView>(R.id.tvAchievementSummary)
        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)

        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        loadData()

        //these cards could be buttons but they cards for rn cuz the nav wont look good
        val cardGamification = findViewById<LinearLayout>(R.id.cardGamification)

        cardGamification.setOnClickListener {
            startActivity(Intent(this, GamificationActivity::class.java))
        }

        val cardAvailable = findViewById<LinearLayout>(R.id.cardAvailable)

        cardAvailable.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        tvAchievementSummary.text =
            "Streak: 7 days • Level: Bronze • Badge: Planner"


        //nav buttons linking to pages
        btnHome.setBackgroundResource(R.drawable.bg_nav_selected)
        btnFinance.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        btnFinanceArrow.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        btnFinance.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        btnHome.setOnClickListener {
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}