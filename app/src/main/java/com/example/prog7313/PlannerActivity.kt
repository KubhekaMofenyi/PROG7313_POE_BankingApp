package com.example.prog7313

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PlannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planner)

        val tvAllocated = findViewById<TextView>(R.id.tvAllocated)
        val tvRemainingPlanner = findViewById<TextView>(R.id.tvRemainingPlanner)
        val cardPlannerWarning = findViewById<LinearLayout>(R.id.cardPlannerWarning)
        val categoryInputs = mutableMapOf<String, EditText>()

        val btnCopyLastMonth = findViewById<Button>(R.id.btnCopyLastMonth)
        val btnSavePlan = findViewById<Button>(R.id.btnSavePlan)
        val btnSkipPlan = findViewById<Button>(R.id.btnSkipPlan)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val categoryContainer = findViewById<LinearLayout>(R.id.categoryContainer)

        val db = AppDatabase.getDatabase(this)
        val budgetDao = db.budgetDao()
        val categoryDao = db.categoryDao()
        val categoryLimitDao = db.categoryLimitDao()

        val etBudget = findViewById<EditText>(R.id.etMonthlyGoal)
        val etMinMonthlyGoal = findViewById<EditText>(R.id.etMinMonthlyGoal)

        fun parseAmount(editText: EditText): Double {
            return editText.text.toString().toDoubleOrNull() ?: 0.0
        }

        fun updateSummary() {
            val monthlyGoal = parseAmount(etBudget)
            val allocated = categoryInputs.values.sumOf { parseAmount(it) }
            tvAllocated.text = "Allocated: R%,.0f".format(allocated)
            tvRemainingPlanner.text = "Remaining: R%,.0f".format(monthlyGoal - allocated)
            cardPlannerWarning.visibility = if (allocated > monthlyGoal && monthlyGoal > 0) View.VISIBLE else View.GONE
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateSummary() }
        }

        lifecycleScope.launch {
            var categories = categoryDao.getAllCategories()
            if (categories.isEmpty()) {
                listOf("Groceries", "Transport", "Bills", "Entertainment", "Other").forEach {
                    categoryDao.insertCategory(Category(name = it))
                }
                categories = categoryDao.getAllCategories()
            }

            val existingLimits = categoryLimitDao.getAllLimits().associate { it.categoryName to it.limitAmount }

            categoryContainer.removeAllViews()
            categories.forEach { category ->
                val label = TextView(this@PlannerActivity).apply {
                    text = category.name
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@PlannerActivity, R.color.text_primary))
                }
                val input = EditText(this@PlannerActivity).apply {
                    hint = "Enter amount"
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    val existingLimit = existingLimits[category.name]
                    if (existingLimit != null && existingLimit > 0) {
                        setText(existingLimit.toInt().toString())
                        hint = "Currently: R${existingLimit.toInt()}"
                    }
                }
                categoryContainer.addView(label)
                categoryContainer.addView(input)
                categoryInputs[category.name] = input
                input.addTextChangedListener(watcher)
            }

            val budget = budgetDao.getBudget()
            if (budget != null) {
                if (budget.monthlyGoal > 0) etBudget.setText(budget.monthlyGoal.toInt().toString())
                if (budget.minMonthlyGoal > 0) etMinMonthlyGoal.setText(budget.minMonthlyGoal.toInt().toString())
            }
            updateSummary()
        }

        btnSkipPlan.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnSavePlan.setOnClickListener {
            val monthlyGoal = parseAmount(etBudget)
            val minMonthlyGoal = parseAmount(etMinMonthlyGoal)

            lifecycleScope.launch {
                budgetDao.insertBudget(
                    Budget(
                        monthlyGoal = monthlyGoal,
                        minMonthlyGoal = minMonthlyGoal,
                        groceriesLimit = 0.0,
                        transportLimit = 0.0,
                        billsLimit = 0.0,
                        entertainmentLimit = 0.0,
                        otherLimit = 0.0
                    )
                )
                categoryInputs.forEach { (category, input) ->
                    val amount = parseAmount(input)
                    categoryLimitDao.saveLimit(CategoryLimit(categoryName = category, limitAmount = amount))
                }
                Toast.makeText(this@PlannerActivity, "Plan saved", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@PlannerActivity, DashboardActivity::class.java))
                finish()
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