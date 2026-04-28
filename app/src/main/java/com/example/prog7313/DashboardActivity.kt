package com.example.prog7313

import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalBalance: TextView
    private lateinit var tvAvailableBalance: TextView
    private lateinit var tvBudgetInfo: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvSafeSpend: TextView
    private lateinit var tvOverspendingMessage: TextView
    private lateinit var tvInsightMessage: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var chipRow: LinearLayout
    private lateinit var cardOverspending: LinearLayout
    private lateinit var tvRecentItem1: TextView
    private lateinit var tvRecentItem2: TextView
    private lateinit var tvRecentItem3: TextView
    private lateinit var tvRecentTip: TextView

    private fun loadData() {
        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()
        val categoryLimitDao = db.categoryLimitDao()

        lifecycleScope.launch {
            val budget = budgetDao.getBudget()
            val totalBalance = budget?.monthlyGoal ?: 0.0
            val totalSpent = expenseDao.getTotalSpent() ?: 0.0
            val remaining = totalBalance - totalSpent
            val safeSpendToday = kotlin.math.max(remaining / 22, 0.0)

            tvTotalBalance.text = "R%,.0f".format(totalBalance)
            tvAvailableBalance.text = "R%,.0f".format(remaining)
            tvBudgetInfo.text = "Spent R%,.0f of R%,.0f".format(totalSpent, totalBalance)
            tvRemaining.text = "R%,.0f".format(remaining)
            tvSafeSpend.text = "R%,.0f".format(safeSpendToday)

            progressBudget.max = totalBalance.toInt()
            progressBudget.progress = totalSpent.toInt()

            // Budget status and overspending message
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

            // Category overspending alerts
            val categoryWarnings = mutableListOf<String>()
            val categoryLimits = categoryLimitDao.getAllLimits()
            for (limit in categoryLimits) {
                val spent = expenseDao.getSpentByCategory(limit.categoryName) ?: 0.0
                if (limit.limitAmount > 0 && spent > limit.limitAmount) {
                    categoryWarnings.add("${limit.categoryName} is over budget by R%,.0f.".format(spent - limit.limitAmount))
                } else if (limit.limitAmount > 0 && spent >= limit.limitAmount * 0.8) {
                    categoryWarnings.add("${limit.categoryName} is close to its limit. R%,.0f remaining.".format(limit.limitAmount - spent))
                }
            }

            if (categoryWarnings.isNotEmpty()) {
                cardOverspending.visibility = View.VISIBLE
                tvOverspendingMessage.text = categoryWarnings.joinToString("\n")
            } else {
                cardOverspending.visibility = View.VISIBLE
                tvOverspendingMessage.text = "No category overspending detected."
            }

            // Top Categories (dynamic)
            val categoryTotals = expenseDao.getCategoryTotals().sortedByDescending { it.total }.take(3)
            chipRow.removeAllViews()
            if (categoryTotals.isEmpty()) {
                val emptyText = TextView(this@DashboardActivity).apply {
                    text = "No expenses yet"
                    setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.text_secondary))
                    textSize = 14f
                }
                chipRow.addView(emptyText)
            } else {
                categoryTotals.forEach { catTotal ->
                    val chip = TextView(this@DashboardActivity).apply {
                        text = catTotal.category
                        setBackgroundResource(R.drawable.bg_chip)
                        setPadding(40, 12, 40, 12)
                        setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.text_primary))
                        textSize = 12f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { marginEnd = 8 }
                    }
                    chipRow.addView(chip)
                }
            }

            // Insights
            val highestCategory = categoryTotals.maxByOrNull { it.total }
            val insight = when {
                categoryTotals.isEmpty() -> "No spending insights yet. Add expenses to get started."
                totalSpent > totalBalance -> "⚠ You have exceeded your monthly budget."
                highestCategory != null && highestCategory.total >= totalSpent * 0.4 -> "You're spending heavily on ${highestCategory.category} this month."
                totalSpent < totalBalance * 0.5 -> "Great job! You're well within your budget."
                else -> "Your spending is balanced across categories."
            }
            tvInsightMessage.text = insight

            // Recent Activity (last 3 expenses)
            val recentExpenses = expenseDao.getAllExpenses().take(3)
            val recentTextViews = listOf(tvRecentItem1, tvRecentItem2, tvRecentItem3)
            for (i in recentTextViews.indices) {
                if (i < recentExpenses.size) {
                    val expense = recentExpenses[i]
                    recentTextViews[i].text = "${expense.category} – R%.0f on ${expense.date}".format(expense.amount)
                    recentTextViews[i].visibility = View.VISIBLE
                } else {
                    recentTextViews[i].visibility = View.GONE
                }
            }

            // Tip (dynamic)
            val tip = when {
                totalSpent > totalBalance -> "Consider reducing non-essential spending."
                remaining < safeSpendToday * 5 -> "You have a tight budget left. Spend carefully."
                else -> "You are on track. Keep it up!"
            }
            tvRecentTip.text = tip

            // Gamification (streak, level, badge)
            val statsDao = db.userStatsDao()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val stats = statsDao.getStats()
            val newStreak = when {
                stats == null -> 1
                stats.lastActiveDate == today -> stats.streak
                else -> stats.streak + 1
            }
            statsDao.saveStats(UserStats(lastActiveDate = today, streak = newStreak))

            val level = when {
                totalSpent < totalBalance * 0.5 -> "Gold"
                totalSpent < totalBalance -> "Silver"
                else -> "Bronze"
            }
            val badge = when {
                newStreak >= 7 -> "Consistency Badge"
                totalSpent < totalBalance * 0.7 -> "Smart Spender"
                else -> "Getting Started"
            }
            findViewById<TextView>(R.id.tvAchievementSummary).text = "Streak: $newStreak days • Level: $level • Badge: $badge"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance)
        tvBudgetInfo = findViewById(R.id.tvBudgetInfo)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvSafeSpend = findViewById(R.id.tvSafeSpend)
        tvOverspendingMessage = findViewById(R.id.tvOverspendingMessage)
        tvInsightMessage = findViewById(R.id.tvInsightMessage)
        progressBudget = findViewById(R.id.progressBudget)
        chipRow = findViewById(R.id.chipRow)
        cardOverspending = findViewById(R.id.cardOverspending)
        tvRecentItem1 = findViewById(R.id.tvRecentItem1)
        tvRecentItem2 = findViewById(R.id.tvRecentItem2)
        tvRecentItem3 = findViewById(R.id.tvRecentItem3)
        tvRecentTip = findViewById(R.id.tvRecentTip)

        val btnFinanceArrow = findViewById<ImageButton>(R.id.btnFinanceArrow)
        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val cardGamification = findViewById<LinearLayout>(R.id.cardGamification)
        val cardAvailable = findViewById<LinearLayout>(R.id.cardAvailable)

        loadData()

        cardGamification.setOnClickListener {
            startActivity(Intent(this, GamificationActivity::class.java))
        }
        cardAvailable.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        btnHome.setBackgroundResource(R.drawable.bg_nav_selected)
        btnFinance.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        btnFinanceArrow.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
        btnFinance.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
        btnHome.setOnClickListener { }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}