package com.example.prog7313

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    //temp..kinda...if it works it works
    private lateinit var expenseDao: ExpenseDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var spCategory: Spinner
    private lateinit var categoryLimitDao: CategoryLimitDao

    var receiptSelected = false

    private var editingExpenseId: Int? = null
    private var selectedReceiptUri: String? = null

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Enter category name"

        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newCategory = input.text.toString().trim()

                if (newCategory.isNotEmpty()) {
                    lifecycleScope.launch {
                        categoryDao.insertCategory(Category(name = newCategory))

                        // reload spinner
                        loadCategories()

                        spCategory.post {
                            val adapter = spCategory.adapter
                            val position = (adapter as ArrayAdapter<String>).getPosition(newCategory)
                            if (position >= 0) {
                                spCategory.setSelection(position)
                            }
                        }

                        Toast.makeText(this@AddExpenseActivity, "Category added", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            var categories = categoryDao.getAllCategories()

            if (categories.isEmpty()) {
                listOf("Groceries", "Transport", "Bills", "Entertainment", "Other").forEach {
                    categoryDao.insertCategory(Category(name = it))
                }

                categories = categoryDao.getAllCategories()
            }

            val categoryNames = categories.map { it.name }

            val spinnerAdapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )

            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategory.adapter = spinnerAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        //database callings
        val db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()
        budgetDao = db.budgetDao()
        categoryDao = db.categoryDao()
        categoryLimitDao = db.categoryLimitDao()

        spCategory = findViewById(R.id.spCategory)

        //da load
        loadCategories()

        //variable callings
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnSaveExpense = findViewById<Button>(R.id.btnSaveExpense)
        val tvCancelExpense = findViewById<TextView>(R.id.tvCancelExpense)
        val cardReceipt = findViewById<LinearLayout>(R.id.cardReceipt)
        val cardWarning = findViewById<LinearLayout>(R.id.cardWarning)
        val tvOverspendWarning = findViewById<TextView>(R.id.tvOverspendWarning)

        //button callings
        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        //extra extra read all about it
        val etNotes = findViewById<EditText>(R.id.etNotes)

        var categoryNames = mutableListOf<String>()
        lifecycleScope.launch {
            var categories = categoryDao.getAllCategories()

            if (categories.isEmpty()) {
                listOf("Groceries", "Transport", "Bills", "Entertainment", "Other").forEach {
                    categoryDao.insertCategory(Category(name = it))
                }
                categories = categoryDao.getAllCategories()
            }

            categoryNames = categories.map { it.name }.toMutableList()
            categoryNames.add("+ Add Category")

            val spinnerAdapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )

            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategory.adapter = spinnerAdapter

            spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                    val selected = categoryNames[position]

                    if (selected == "+ Add Category") {
                        showAddCategoryDialog()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            val selectedCategory = intent.getStringExtra("category")
            val categoryIndex = categoryNames.indexOf(selectedCategory)

            if (categoryIndex >= 0) {
                spCategory.setSelection(categoryIndex)
            }
        }

        fun updateWarning() {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem?.toString() ?: return

            if (amount <= 0.0) {
                cardWarning.visibility = View.GONE
                return
            }

            lifecycleScope.launch {
                val budget = budgetDao.getBudget()

                val categoryLimit = categoryLimitDao.getLimitForCategory(category)
                val limit = categoryLimit?.limitAmount ?: 0.0

                val spent = expenseDao.getSpentByCategory(category) ?: 0.0
                val projected = spent + amount
                val remaining = limit - projected

                if (limit <= 0.0) {
                    cardWarning.visibility = View.GONE
                    return@launch
                }

                when {
                    remaining < 0 -> {
                        cardWarning.visibility = View.VISIBLE
                        cardWarning.setBackgroundResource(R.drawable.bg_card_secondary)
                        tvOverspendWarning.setTextColor(Color.WHITE)
                        tvOverspendWarning.text =
                            "Overspending! You exceed $category by R%.0f.".format(-remaining)
                    }

                    remaining <= limit * 0.2 -> {
                        cardWarning.visibility = View.VISIBLE
                        cardWarning.setBackgroundResource(R.drawable.bg_card_secondary)
                        tvOverspendWarning.setTextColor(Color.WHITE)
                        tvOverspendWarning.text =
                            "Warning: Only R%.0f left in $category after this expense.".format(remaining)
                    }

                    else -> {
                        cardWarning.visibility = View.GONE
                    }
                }
            }
        }

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWarning()
            }
        })

        spCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateWarning()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    etDate.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val pickImageLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                selectedReceiptUri = it.toString()

                cardWarning.visibility = View.VISIBLE
                cardWarning.setBackgroundResource(R.drawable.bg_card_white)
                tvOverspendWarning.text = "Receipt attached."
            }
        }

        cardReceipt.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        editingExpenseId = intent.getIntExtra("expenseId", -1).takeIf { it != -1 }

        if (editingExpenseId != null) {
            etAmount.setText(intent.getDoubleExtra("amount", 0.0).toString())
            etDate.setText(intent.getStringExtra("date"))
            etNotes.setText(intent.getStringExtra("notes"))

            val selectedCategory = intent.getStringExtra("category")

            btnSaveExpense.text = "Update Expense"
        }

        btnSaveExpense.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem.toString()
            val date = etDate.text.toString()
            val notes = etNotes.text.toString()

            lifecycleScope.launch {
                if (editingExpenseId != null) {
                    val updatedExpense = Expense(
                        id = editingExpenseId!!,
                        amount = amount,
                        category = category,
                        date = date,
                        notes = notes,
                        receiptUri = selectedReceiptUri
                    )
                    expenseDao.updateExpense(updatedExpense)
                    Toast.makeText(this@AddExpenseActivity, "Expense updated", Toast.LENGTH_SHORT).show()
                } else {
                    val newExpense = Expense(
                        amount = amount,
                        category = category,
                        date = date,
                        notes = notes,
                        receiptUri = selectedReceiptUri
                    )
                    expenseDao.insertExpense(newExpense)
                    Toast.makeText(this@AddExpenseActivity, "Expense added", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
        }

        tvCancelExpense.setOnClickListener {
            finish()
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

        cardWarning.visibility = View.GONE
    }
}