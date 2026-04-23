package com.example.prog7313

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

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

        val categories = listOf("Groceries", "Transport", "Bills", "Entertainment", "Other")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = spinnerAdapter

        // Sample prototype data
        val categoryLimits = mapOf(
            "Groceries" to 1200.0,
            "Transport" to 800.0,
            "Bills" to 1500.0,
            "Entertainment" to 700.0,
            "Other" to 300.0
        )

        val currentSpent = mapOf(
            "Groceries" to 850.0,
            "Transport" to 420.0,
            "Bills" to 1300.0,
            "Entertainment" to 200.0,
            "Other" to 100.0
        )

        fun updateWarning() {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = spCategory.selectedItem?.toString() ?: return

            if (amount <= 0.0) {
                cardWarning.visibility = View.GONE
                return
            }

            val limit = categoryLimits[category] ?: 0.0
            val spent = currentSpent[category] ?: 0.0
            val projected = spent + amount
            val remaining = limit - projected

            cardWarning.visibility = View.VISIBLE

            when {
                remaining < 0 -> {
                    cardWarning.setBackgroundResource(R.drawable.bg_card_secondary)
                    tvOverspendWarning.setTextColor(Color.WHITE)
                    tvOverspendWarning.text =
                        "Overspending warning. This will exceed $category by R%.0f.".format(-remaining)
                }

                remaining <= 100 -> {
                    cardWarning.setBackgroundResource(R.drawable.bg_card_secondary)
                    tvOverspendWarning.setTextColor(Color.WHITE)
                    tvOverspendWarning.text =
                        "Approaching limit. R%.0f will remain in $category after this expense.".format(remaining)
                }

                else -> {
                    cardWarning.setBackgroundResource(R.drawable.bg_card_white)
                    tvOverspendWarning.setTextColor(
                        ContextCompat.getColor(this, R.color.text_primary)
                    )
                    tvOverspendWarning.text =
                        "Within budget. R%.0f will remain in $category after this expense.".format(remaining)
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

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateWarning()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
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

        cardReceipt.setOnClickListener {
            cardWarning.visibility = View.VISIBLE
            cardWarning.setBackgroundResource(R.drawable.bg_card_white)
            tvOverspendWarning.setTextColor(
                ContextCompat.getColor(this, R.color.text_primary)
            )
            tvOverspendWarning.text = "Receipt upload placeholder selected."
        }

        btnSaveExpense.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
            finish()
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