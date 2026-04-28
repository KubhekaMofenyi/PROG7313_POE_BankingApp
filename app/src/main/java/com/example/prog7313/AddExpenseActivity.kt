package com.example.prog7313

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

    private var editingExpenseId: Int? = null
    private var selectedReceiptUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnSaveExpense = findViewById<Button>(R.id.btnSaveExpense)
        val tvCancelExpense = findViewById<TextView>(R.id.tvCancelExpense)
        val cardReceipt = findViewById<LinearLayout>(R.id.cardReceipt)
        val cardWarning = findViewById<LinearLayout>(R.id.cardWarning)
        val tvOverspendWarning = findViewById<TextView>(R.id.tvOverspendWarning)

        val btnFinance = findViewById<ImageButton>(R.id.btnFinance)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val etNotes = findViewById<EditText>(R.id.etNotes)
        var receiptSelected = false

        val db = AppDatabase.getDatabase(this)
        val expenseDao = db.expenseDao()
        val budgetDao = db.budgetDao()

        val categories = listOf("Groceries", "Transport", "Bills", "Entertainment", "Other")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = spinnerAdapter

        fun updateWarning() {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem?.toString() ?: return

            if (amount <= 0.0) {
                cardWarning.visibility = View.GONE
                return
            }

            lifecycleScope.launch {
                val budget = budgetDao.getBudget()

                val limit = when (category) {
                    "Groceries" -> budget?.groceriesLimit ?: 0.0
                    "Transport" -> budget?.transportLimit ?: 0.0
                    "Bills" -> budget?.billsLimit ?: 0.0
                    "Entertainment" -> budget?.entertainmentLimit ?: 0.0
                    "Other" -> budget?.otherLimit ?: 0.0
                    else -> 0.0
                }

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
            val categoryIndex = categories.indexOf(selectedCategory)

            if (categoryIndex >= 0) {
                spCategory.setSelection(categoryIndex)
            }

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