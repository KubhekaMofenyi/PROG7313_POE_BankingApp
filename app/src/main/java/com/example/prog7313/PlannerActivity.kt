package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PlannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planner)

        val etMonthlyGoal = findViewById<EditText>(R.id.etMonthlyGoal)
        val etGroceries = findViewById<EditText>(R.id.etGroceries)
        val etTransport = findViewById<EditText>(R.id.etTransport)
        val etBills = findViewById<EditText>(R.id.etBills)
        val etEntertainment = findViewById<EditText>(R.id.etEntertainment)
        val etOther = findViewById<EditText>(R.id.etOther)

        val tvAllocated = findViewById<TextView>(R.id.tvAllocated)
        val tvRemainingPlanner = findViewById<TextView>(R.id.tvRemainingPlanner)
        val cardPlannerWarning = findViewById<LinearLayout>(R.id.cardPlannerWarning)

        val btnCopyLastMonth = findViewById<Button>(R.id.btnCopyLastMonth)
        val btnSavePlan = findViewById<Button>(R.id.btnSavePlan)
        val btnSkipPlan = findViewById<Button>(R.id.btnSkipPlan)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val db = AppDatabase.getDatabase(this)
        val budgetDao = db.budgetDao()

        val etBudget = findViewById<EditText>(R.id.etMonthlyGoal)
        val btnSave = findViewById<Button>(R.id.btnSavePlan)

        fun parseAmount(editText: EditText): Double {
            return editText.text.toString().toDoubleOrNull() ?: 0.0
        }

        fun updateSummary() {
            val monthlyGoal = parseAmount(etMonthlyGoal)
            val allocated =
                parseAmount(etGroceries) +
                        parseAmount(etTransport) +
                        parseAmount(etBills) +
                        parseAmount(etEntertainment) +
                        parseAmount(etOther)

            val remaining = monthlyGoal - allocated

            tvAllocated.text = "Allocated: R%,.0f".format(allocated)
            tvRemainingPlanner.text = "Remaining: R%,.0f".format(remaining)

            cardPlannerWarning.visibility = if (allocated > monthlyGoal && monthlyGoal > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSummary()
            }
        }

        listOf(etMonthlyGoal, etGroceries, etTransport, etBills, etEntertainment, etOther)
            .forEach { it.addTextChangedListener(watcher) }

        btnCopyLastMonth.setOnClickListener {
            etMonthlyGoal.setText("5000")
            etGroceries.setText("1200")
            etTransport.setText("800")
            etBills.setText("1500")
            etEntertainment.setText("700")
            etOther.setText("300")
        }

        btnSavePlan.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnSkipPlan.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnSavePlan.setOnClickListener {
            val budgetValue = parseAmount(etMonthlyGoal)

            if (budgetValue > 0) {
                lifecycleScope.launch {
                    budgetDao.insertBudget(Budget(monthlyGoal = budgetValue))
                    Toast.makeText(this@PlannerActivity, "Budget saved", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PlannerActivity, FinanceActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, "Please enter a monthly goal", Toast.LENGTH_SHORT).show()
            }
        }

        btnHome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        btnFinance.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
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

        cardPlannerWarning.visibility = View.GONE
        updateSummary()
    }
}