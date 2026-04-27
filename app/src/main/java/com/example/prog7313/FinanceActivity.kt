package com.example.prog7313

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class FinanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        val pieChart = findViewById<PieChart>(R.id.expensePieChart)
        val barChart = findViewById<BarChart>(R.id.spendingBarChart)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val cardQuickAddExpense = findViewById<LinearLayout>(R.id.cardQuickAddExpense)
        val cardMonthlyPlanner = findViewById<LinearLayout>(R.id.cardMonthlyPlanner)

        val tvTotal = findViewById<TextView>(R.id.tvFinanceTotal)
        val tvAvailable = findViewById<TextView>(R.id.tvFinanceAvailable)

        val spendingBarChart = findViewById<BarChart>(R.id.spendingBarChart)

        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        //pie chart
        lifecycleScope.launch {
            val categoryTotals = expenseDao.getCategoryTotals()

            //percentages
            val total = categoryTotals.sumOf { it.total }

            //categories
            val categoryListLayout = findViewById<LinearLayout>(R.id.cardCategoryList)
            categoryListLayout.removeAllViews()

            categoryTotals.forEach {
                val textView = TextView(this@FinanceActivity)
                val percentage = if (total > 0) (it.total / total) * 100 else 0.0
                textView.text = "${it.category} – R%,.0f (%.0f%%)".format(it.total, percentage)
                textView.setTextColor(Color.parseColor("#FDEDE9"))
                textView.textSize = 14f

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = 10

                textView.layoutParams = params

                categoryListLayout.addView(textView)

                val maxCategory = categoryTotals.maxByOrNull { it.total }

                if (it == maxCategory) {
                    textView.setTypeface(null, Typeface.BOLD)
                }
            }

            //pie chart
            val entries = categoryTotals.map {
                PieEntry(it.total.toFloat(), it.category)
            }

            val dataSet = PieDataSet(entries, "Expenses")

            dataSet.colors = listOf(
                Color.parseColor("#8E210E"),
                Color.parseColor("#C77921"),
                Color.parseColor("#6B2E17"),
                Color.parseColor("#A64E2D")
            )

            dataSet.valueTextColor = Color.WHITE
            dataSet.valueTextSize = 12f

            val pieData = PieData(dataSet)

            pieChart.data = pieData
            pieChart.description.isEnabled = false
            pieChart.centerText = "Expenses"
            pieChart.invalidate()
        }

        //bar chart
        lifecycleScope.launch {
            val dailyTotals = expenseDao.getDailyTotals()
            val labels = dailyTotals.map { it.date }

            val entries = dailyTotals.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.total.toFloat())
            }

            val dataSet = BarDataSet(entries, "Daily Spend")
            dataSet.color = Color.parseColor("#C77921")
            dataSet.valueTextColor = Color.parseColor("#1F1F1F")

            val barData = BarData(dataSet)

            spendingBarChart.data = barData
            val xAxis = spendingBarChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

            spendingBarChart.description.isEnabled = false
            spendingBarChart.invalidate()
        }

        loadData()

        btnFinance.setBackgroundResource(R.drawable.bg_nav_selected)
        btnHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        cardQuickAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        cardMonthlyPlanner.setOnClickListener {
            startActivity(Intent(this, PlannerActivity::class.java))
        }

        val cardExpenseHistory = findViewById<LinearLayout>(R.id.cardExpenseHistory)

        cardExpenseHistory.setOnClickListener {
            startActivity(Intent(this, ExpenseHistoryActivity::class.java))
        }

        btnFinance.setOnClickListener {
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    private fun loadData() {
        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        lifecycleScope.launch {
            val budget = budgetDao.getBudget()
            val totalBalance = budget?.monthlyGoal ?: 0.0

            val totalSpent = expenseDao.getTotalSpent() ?: 0.0
            val remaining = totalBalance - totalSpent

            val tvTotal = findViewById<TextView>(R.id.tvFinanceTotal)
            val tvAvailable = findViewById<TextView>(R.id.tvFinanceAvailable)

            tvTotal.text = "R%,.0f".format(totalBalance)
            tvAvailable.text = "R%,.0f".format(remaining)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}