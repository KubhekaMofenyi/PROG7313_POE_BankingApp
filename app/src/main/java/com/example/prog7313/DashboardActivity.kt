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
import kotlin.math.max

class DashboardActivity : AppCompatActivity() {

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
        val tvInsightMessage = findViewById<TextView>(R.id.tvInsightMessage)
        val tvAchievementSummary = findViewById<TextView>(R.id.tvAchievementSummary)
        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)

        val totalBalance = 10000
        val monthlyBudget = 5000
        val spent = 1850
        val remainingBudget = monthlyBudget - spent
        val availableToSpend = totalBalance - spent
        val daysLeftInMonth = 22
        val safeSpendToday = max(remainingBudget / daysLeftInMonth, 0)

        //these cards could be buttons but they cards for rn cuz the nav wont look good
        val cardGamification = findViewById<LinearLayout>(R.id.cardGamification)

        cardGamification.setOnClickListener {
            startActivity(Intent(this, GamificationActivity::class.java))
        }

        val cardAvailable = findViewById<LinearLayout>(R.id.cardAvailable)

        cardAvailable.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        //the math for the budgetting as of now
        tvTotalBalance.text = "R%,d".format(totalBalance)
        tvAvailableBalance.text = "R%,d".format(availableToSpend)
        tvBudgetInfo.text = "Spent R%,d of R%,d".format(spent, monthlyBudget)
        tvRemaining.text = "R%,d".format(remainingBudget)
        tvSafeSpend.text = "R%,d".format(safeSpendToday)

        progressBudget.max = monthlyBudget
        progressBudget.progress = spent

        when {
            spent < monthlyBudget * 0.7 -> {
                tvBudgetStatus.text = "Excellent. You are well within budget."
                tvBudgetStatus.setTextColor(Color.parseColor("#2E7D32"))
                tvRecentTip.text = "Tip: You are spending responsibly this month."
            }
            spent <= monthlyBudget -> {
                tvBudgetStatus.text = "You are managing your budget well this month."
                tvBudgetStatus.setTextColor(Color.parseColor("#EF6C00"))
                tvRecentTip.text = "Tip: Watch your spending in the next few days."
            }
            else -> {
                tvBudgetStatus.text = "Warning: you are over your monthly budget."
                tvBudgetStatus.setTextColor(Color.parseColor("#C62828"))
                tvRecentTip.text = "Tip: Reduce non-essential spending this week."
            }
        }

        val overspendingCategory = "Transport"
        val overspendingAmount = 150

        tvOverspendingMessage.text =
            "$overspendingCategory has exceeded its monthly category limit by R$overspendingAmount."

        tvInsightMessage.text =
            "You spend more on weekends than weekdays."

        tvAchievementSummary.text =
            "Streak: 7 days • Level: Bronze • Badge: Planner"

        //overspending
        val cardOverspending = findViewById<LinearLayout>(R.id.cardOverspending)

        if (overspendingAmount > 0) {
            cardOverspending.visibility = android.view.View.VISIBLE
        } else {
            cardOverspending.visibility = android.view.View.GONE
        }

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
}