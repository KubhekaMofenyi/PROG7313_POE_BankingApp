package com.example.prog7313

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class FinanceActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tvTotal: TextView
    private lateinit var tvAvailable: TextView
    private lateinit var categoryListLayout: LinearLayout
    private lateinit var budgetProgressContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        pieChart = findViewById(R.id.expensePieChart)
        barChart = findViewById(R.id.spendingBarChart)
        tvTotal = findViewById(R.id.tvFinanceTotal)
        tvAvailable = findViewById(R.id.tvFinanceAvailable)
        categoryListLayout = findViewById(R.id.cardCategoryList)
        budgetProgressContainer = findViewById(R.id.budgetProgressContainer)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        btnFinance.setBackgroundResource(R.drawable.bg_nav_selected)
        btnHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        findViewById<LinearLayout>(R.id.cardQuickAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardMonthlyPlanner).setOnClickListener {
            startActivity(Intent(this, PlannerActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardExpenseHistory).setOnClickListener {
            startActivity(Intent(this, ExpenseHistoryActivity::class.java))
        }

        btnFinance.setOnClickListener { }
        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        loadData()
    }

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

            tvTotal.text = "R%,.0f".format(totalBalance)
            tvAvailable.text = "R%,.0f".format(remaining)

            val categoryTotals = expenseDao.getCategoryTotals()

            // Pie Chart
            val pieEntries = categoryTotals.map { PieEntry(it.total.toFloat(), it.category) }
            val pieDataSet = PieDataSet(pieEntries, "Expenses")
            pieDataSet.colors = listOf(
                Color.parseColor("#8E210E"), Color.parseColor("#C77921"),
                Color.parseColor("#6B2E17"), Color.parseColor("#A64E2D")
            )
            pieDataSet.valueTextColor = Color.WHITE
            pieDataSet.valueTextSize = 12f
            pieChart.data = PieData(pieDataSet)
            pieChart.description.isEnabled = false
            pieChart.centerText = "Expenses"
            pieChart.invalidate()

            // Bar Chart (daily spending)
            val dailyTotals = expenseDao.getDailyTotals()
            val barEntries = dailyTotals.mapIndexed { index, item -> BarEntry(index.toFloat(), item.total.toFloat()) }
            val barDataSet = BarDataSet(barEntries, "Daily Spend")
            barDataSet.color = Color.parseColor("#C77921")
            barDataSet.valueTextColor = Color.parseColor("#1F1F1F")
            barChart.data = BarData(barDataSet)
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dailyTotals.map { it.date })
            barChart.xAxis.granularity = 1f
            barChart.description.isEnabled = false
            barChart.invalidate()

            // Clickable category totals with percentages
            categoryListLayout.removeAllViews()
            val totalSpentAll = categoryTotals.sumOf { it.total }
            categoryTotals.forEach { catTotal ->
                val percentage = if (totalSpentAll > 0) (catTotal.total / totalSpentAll) * 100 else 0.0
                val textView = TextView(this@FinanceActivity).apply {
                    text = "${catTotal.category} – R%,.0f (%.1f%%)".format(catTotal.total, percentage)
                    setTextColor(Color.parseColor("#FDEDE9"))
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                    setOnClickListener {
                        val intent = Intent(this@FinanceActivity, ExpenseHistoryActivity::class.java)
                        intent.putExtra("presetCategory", catTotal.category)
                        startActivity(intent)
                    }
                }
                categoryListLayout.addView(textView)
            }

            // Budget vs Actual – show ALL categories that have a limit (including zero spending)
            val allCategoriesWithLimits = categoryLimitDao.getAllLimits() // includes all that ever had a limit
            val existingCategoryNames = categoryTotals.map { it.category }.toSet()
            budgetProgressContainer.removeAllViews()


            allCategoriesWithLimits.forEach { limit ->
                val spent = categoryTotals.find { it.category == limit.categoryName }?.total ?: 0.0
                val textView = TextView(this@FinanceActivity).apply {
                    text = "${limit.categoryName}: R%.0f / R%.0f".format(spent, limit.limitAmount)
                    textSize = 14f
                    when {
                        limit.limitAmount == 0.0 -> setTextColor(Color.GRAY)
                        spent >= limit.limitAmount -> setTextColor(Color.RED)
                        spent > limit.limitAmount * 0.8 -> setTextColor(Color.parseColor("#FF9800"))
                        else -> setTextColor(Color.parseColor("#2E7D32"))
                    }
                }
                budgetProgressContainer.addView(textView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}