package com.example.prog7313

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

class ExpenseHistoryActivity : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var listView: ListView
    private lateinit var etSearch: EditText
    private lateinit var spCategoryFilter: Spinner
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnApplyFilter: Button
    private lateinit var btnClearFilters: Button

    private lateinit var allExpenses: List<Expense>   // full list from DB
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        etSearch = findViewById(R.id.etSearch)
        spCategoryFilter = findViewById(R.id.spCategoryFilter)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        btnClearFilters = findViewById(R.id.btnClearFilters)
        listView = findViewById(R.id.listExpenses)

        val db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()
        categoryDao = db.categoryDao()

        setupDatePickers()      // no auto-apply inside
        loadCategorySpinner()
        loadAllExpenses()       // load once

        // Real-time keyword search (optional, but convenient)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters()   // apply on each keystroke
            }
        })

        btnApplyFilter.setOnClickListener {
            applyFilters()
        }

        btnClearFilters.setOnClickListener {
            etSearch.setText("")
            spCategoryFilter.setSelection(0)
            etStartDate.setText("")
            etEndDate.setText("")
            applyFilters()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val adapter = listView.adapter as? ExpenseAdapter
            val selectedExpense = adapter?.getItem(position)
            if (selectedExpense != null) {
                showExpenseOptions(selectedExpense)
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val adapter = listView.adapter as? ExpenseAdapter
            val selectedExpense = adapter?.getItem(position)
            if (selectedExpense != null) {
                confirmDelete(selectedExpense)
                return@setOnItemLongClickListener true
            }
            false
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val dateListener = { editText: EditText ->
            DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                    editText.setText(date)
                    // Do NOT call applyFilters() here – wait for user to click Apply
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        etStartDate.setOnClickListener { dateListener(etStartDate) }
        etEndDate.setOnClickListener { dateListener(etEndDate) }
    }

    private fun loadCategorySpinner() {
        lifecycleScope.launch {
            val categories = categoryDao.getAllCategories().map { it.name }.toMutableList()
            categories.add(0, "All Categories")
            val adapter = ArrayAdapter(this@ExpenseHistoryActivity, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategoryFilter.adapter = adapter
        }
    }

    private fun loadAllExpenses() {
        lifecycleScope.launch {
            allExpenses = expenseDao.getAllExpenses().sortedByDescending { it.date }
            applyFilters()  // initial display
        }
    }

    private fun applyFilters() {
        if (!::allExpenses.isInitialized) return

        val keyword = etSearch.text.toString().trim().lowercase()
        val selectedCategory = spCategoryFilter.selectedItem?.toString()
        val category = if (selectedCategory == null || selectedCategory == "All Categories") null else selectedCategory
        val startDateStr = etStartDate.text.toString().trim()
        val endDateStr = etEndDate.text.toString().trim()

        // Parse start and end dates if provided
        val startDate = try {
            if (startDateStr.isNotEmpty()) dateFormat.parse(startDateStr) else null
        } catch (e: Exception) { null }
        val endDate = try {
            if (endDateStr.isNotEmpty()) dateFormat.parse(endDateStr) else null
        } catch (e: Exception) { null }

        val filtered = allExpenses.filter { expense ->
            // keyword filter
            val matchesKeyword = keyword.isEmpty() ||
                    expense.category.lowercase().contains(keyword) ||
                    expense.notes.lowercase().contains(keyword) ||
                    expense.date.lowercase().contains(keyword)

            // category filter
            val matchesCategory = category == null || expense.category == category

            // date range filter
            val expenseDate = try {
                dateFormat.parse(expense.date)
            } catch (e: Exception) { null }

            val matchesDateRange = when {
                expenseDate == null -> false
                startDate != null && endDate != null -> expenseDate >= startDate && expenseDate <= endDate
                startDate != null -> expenseDate >= startDate
                endDate != null -> expenseDate <= endDate
                else -> true
            }

            matchesKeyword && matchesCategory && matchesDateRange
        }

        listView.adapter = ExpenseAdapter(this, filtered)
    }

    private fun showExpenseOptions(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Expense Options")
            .setMessage(
                "Category: ${expense.category}\n" +
                        "Amount: R%,.0f\n".format(expense.amount) +
                        "Date: ${expense.date}"
            )
            .setPositiveButton("Edit") { _, _ ->
                val intent = Intent(this, AddExpenseActivity::class.java)
                intent.putExtra("expenseId", expense.id)
                intent.putExtra("amount", expense.amount)
                intent.putExtra("category", expense.category)
                intent.putExtra("date", expense.date)
                intent.putExtra("notes", expense.notes)
                intent.putExtra("receiptUri", expense.receiptUri)
                startActivity(intent)
            }
            .setNeutralButton("Delete") { _, _ ->
                confirmDelete(expense)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ ->
                deleteExpense(expense)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteExpense(expense: Expense) {
        lifecycleScope.launch {
            expenseDao.deleteExpenseById(expense.id)
            Toast.makeText(this@ExpenseHistoryActivity, "Expense deleted", Toast.LENGTH_SHORT).show()
            loadAllExpenses() // reload and re-filter
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategorySpinner()
        loadAllExpenses() // refresh when returning from edit
    }
}