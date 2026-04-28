package com.example.prog7313

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryLimitDao: CategoryLimitDao
    private lateinit var spCategory: Spinner

    private var editingExpenseId: Int? = null
    private var selectedReceiptUri: String? = null
    private var categoryNames = mutableListOf<String>()

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
                        loadCategories(newCategory)
                        Toast.makeText(this@AddExpenseActivity, "Category added", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadCategories(selectCategory: String? = null) {
        lifecycleScope.launch {
            var categories = categoryDao.getAllCategories()

            if (categories.isEmpty()) {
                val defaultColors = mapOf(
                    "Groceries" to "#81C784",
                    "Transport" to "#64B5F6",
                    "Bills" to "#E57373",
                    "Entertainment" to "#BA68C8",
                    "Other" to "#FFB74D"
                )
                defaultColors.forEach { (name, color) ->
                    categoryDao.insertCategory(Category(name = name, color = color))
                }
                categoryDao.insertCategory(Category(name = "Uncategorised", color = "#9E9E9E"))
            }

            if (categoryDao.getCategoryByName("Uncategorised") == null) {
                categoryDao.insertCategory(Category(name = "Uncategorised", color = "#9E9E9E"))
            }

            categoryNames = categories.map { it.name }.distinct().toMutableList()
            categoryNames.add("+ Add Category")

            val spinnerAdapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )

            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategory.adapter = spinnerAdapter

            selectCategory?.let {
                val index = categoryNames.indexOf(it)
                if (index >= 0) spCategory.setSelection(index)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()
        budgetDao = db.budgetDao()
        categoryDao = db.categoryDao()
        categoryLimitDao = db.categoryLimitDao()

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        spCategory = findViewById(R.id.spCategory)

        val btnSaveExpense = findViewById<Button>(R.id.btnSaveExpense)
        val tvCancelExpense = findViewById<TextView>(R.id.tvCancelExpense)
        val cardReceipt = findViewById<LinearLayout>(R.id.cardReceipt)
        val cardWarning = findViewById<LinearLayout>(R.id.cardWarning)
        val tvOverspendWarning = findViewById<TextView>(R.id.tvOverspendWarning)
        val etNotes = findViewById<EditText>(R.id.etNotes)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        fun updateWarning() {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem?.toString() ?: return

            if (amount <= 0.0 || category == "+ Add Category") {
                cardWarning.visibility = View.GONE
                return
            }

            lifecycleScope.launch {
                val categoryLimit = categoryLimitDao.getLimitForCategory(category)
                val limit = categoryLimit?.limitAmount ?: 0.0

                if (limit <= 0.0) {
                    cardWarning.visibility = View.GONE
                    return@launch
                }

                val spent = expenseDao.getSpentByCategory(category) ?: 0.0
                val projected = spent + amount
                val remaining = limit - projected

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



        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = categoryNames.getOrNull(position) ?: return

                if (selected == "+ Add Category") {
                    showAddCategoryDialog()
                } else {
                    updateWarning()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        editingExpenseId = intent.getIntExtra("expenseId", -1).takeIf { it != -1 }

        val selectedCategoryFromIntent = intent.getStringExtra("category")
        loadCategories(selectedCategoryFromIntent)

        if (editingExpenseId != null) {
            etAmount.setText(intent.getDoubleExtra("amount", 0.0).toString())
            etDate.setText(intent.getStringExtra("date"))
            etNotes.setText(intent.getStringExtra("notes"))
            selectedReceiptUri = intent.getStringExtra("receiptUri")
            btnSaveExpense.text = "Update Expense"
        }

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWarning()
            }
        })

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
            androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                selectedReceiptUri = it.toString()

                cardWarning.visibility = View.VISIBLE
                cardWarning.setBackgroundResource(R.drawable.bg_card_white)
                tvOverspendWarning.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
                )
                tvOverspendWarning.text = "Receipt attached."
            }
        }

        cardReceipt.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSaveExpense.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem?.toString() ?: ""
            val date = etDate.text.toString()
            val notes = etNotes.text.toString()

            if (amount <= 0.0 || category.isBlank() || category == "+ Add Category" || date.isBlank()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (editingExpenseId != null) {
                    expenseDao.updateExpense(
                        Expense(
                            id = editingExpenseId!!,
                            amount = amount,
                            category = category,
                            date = date,
                            notes = notes,
                            receiptUri = selectedReceiptUri
                        )
                    )
                    Toast.makeText(this@AddExpenseActivity, "Expense updated", Toast.LENGTH_SHORT).show()
                } else {
                    expenseDao.insertExpense(
                        Expense(
                            amount = amount,
                            category = category,
                            date = date,
                            notes = notes,
                            receiptUri = selectedReceiptUri
                        )
                    )
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

    override fun onResume() {
        super.onResume()
        // Reload categories, preserve selection if possible
        val currentSelection = if (spCategory.selectedItemPosition >= 0) {
            spCategory.selectedItem.toString()
        } else null
        loadCategories(currentSelection)
    }
}