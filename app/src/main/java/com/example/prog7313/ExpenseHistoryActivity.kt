package com.example.prog7313

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ExpenseHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        val listView = findViewById<ListView>(R.id.listExpenses)

        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()

        lifecycleScope.launch {
            val expenses = expenseDao.getAllExpenses()

            val displayList = expenses.map {
                "R${it.amount} | ${it.category} | ${it.date}"
            }

            val adapter = android.widget.ArrayAdapter(
                this@ExpenseHistoryActivity,
                android.R.layout.simple_list_item_1,
                displayList
            )

            listView.adapter = adapter
        }
    }
}