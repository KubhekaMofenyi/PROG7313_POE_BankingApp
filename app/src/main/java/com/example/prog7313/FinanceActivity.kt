package com.example.prog7313

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

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

        val pieEntries = arrayListOf<PieEntry>(
            PieEntry(850f, "Groceries"),
            PieEntry(420f, "Transport"),
            PieEntry(1300f, "Bills"),
            PieEntry(200f, "Entertainment")
        )

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = listOf(
            Color.parseColor("#8E210E"),
            Color.parseColor("#C97921"),
            Color.parseColor("#6B2B17"),
            Color.parseColor("#A64E2D")
        )
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 12f

        pieChart.data = PieData(pieDataSet)
        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.setCenterTextSize(16f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.invalidate()

        val barEntries = arrayListOf<BarEntry>(
            BarEntry(1f, 120f),
            BarEntry(2f, 80f),
            BarEntry(3f, 150f),
            BarEntry(4f, 60f),
            BarEntry(5f, 200f),
            BarEntry(6f, 300f),
            BarEntry(7f, 250f)
        )

        val barDataSet = BarDataSet(barEntries, "Daily Spend")
        barDataSet.color = Color.parseColor("#C97921")
        barDataSet.valueTextColor = Color.parseColor("#1F1F1F")
        barDataSet.valueTextSize = 10f

        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.invalidate()

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
}