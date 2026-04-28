package com.example.prog7313

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ExpenseHistoryActivity : AppCompatActivity() {

    private lateinit var expenseDao: ExpenseDao
    private lateinit var listView: ListView
    private var expenses: List<Expense> = emptyList()

    lateinit var etSearch: EditText

    private fun loadExpenses() {
        lifecycleScope.launch {
            expenses = expenseDao.getAllExpenses()

            val adapter = ExpenseAdapter(this@ExpenseHistoryActivity, expenses)
            listView.adapter = adapter

            expenses = expenseDao.getAllExpenses()
                .sortedByDescending { it.date } // newest first

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString().lowercase()

                    val filtered = expenses.filter {
                        it.category.lowercase().contains(query) ||
                                it.date.lowercase().contains(query) ||
                                it.notes.lowercase().contains(query)
                    }

                    listView.adapter = ExpenseAdapter(this@ExpenseHistoryActivity, filtered)
                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        etSearch = findViewById(R.id.etSearch)

        listView = findViewById(R.id.listExpenses)

        val db = AppDatabase.getDatabase(this)
        expenseDao = db.expenseDao()

        loadExpenses()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedExpense = expenses[position]

            AlertDialog.Builder(this)
                .setTitle("Expense Options")
                .setMessage(
                    "Category: ${selectedExpense.category}\n" +
                            "Amount: R%,.0f\n".format(selectedExpense.amount) +
                            "Date: ${selectedExpense.date}"
                )
                .setPositiveButton("Edit") { _, _ ->
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    intent.putExtra("expenseId", selectedExpense.id)
                    intent.putExtra("amount", selectedExpense.amount)
                    intent.putExtra("category", selectedExpense.category)
                    intent.putExtra("date", selectedExpense.date)
                    intent.putExtra("notes", selectedExpense.notes)
                    startActivity(intent)
                }
                .setNeutralButton("Delete") { _, _ ->
                    deleteExpense(selectedExpense)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedExpense = expenses[position]

            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("expenseId", selectedExpense.id)
            intent.putExtra("amount", selectedExpense.amount)
            intent.putExtra("category", selectedExpense.category)
            intent.putExtra("date", selectedExpense.date)
            intent.putExtra("notes", selectedExpense.notes)

            startActivity(intent)

            AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteExpense(selectedExpense)
                }
                .setNegativeButton("No", null)
                .show()

            true
        }
    }

    private fun deleteExpense(expense: Expense) {
        lifecycleScope.launch {
            expenseDao.deleteExpenseById(expense.id)
            Toast.makeText(this@ExpenseHistoryActivity, "Expense deleted", Toast.LENGTH_SHORT).show()
            loadExpenses()
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }
}