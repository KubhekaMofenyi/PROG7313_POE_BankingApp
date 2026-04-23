package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ExpenseHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        val spPeriodFilter = findViewById<Spinner>(R.id.spPeriodFilter)
        val spCategoryFilter = findViewById<Spinner>(R.id.spCategoryFilter)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val periodOptions = listOf("Daily", "Weekly", "Monthly")
        val categoryOptions = listOf("All Categories", "Groceries", "Transport", "Bills", "Entertainment", "Other")

        val periodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodOptions)
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPeriodFilter.adapter = periodAdapter

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryOptions)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoryFilter.adapter = categoryAdapter

        btnHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnFinance.setBackgroundResource(R.drawable.bg_nav_selected)
        btnSettings.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

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