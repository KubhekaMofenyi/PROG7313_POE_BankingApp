package com.example.prog7313

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ExpenseAdapter(
    context: Context,
    private val expenses: List<Expense>
) : ArrayAdapter<Expense>(context, 0, expenses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_expense, parent, false)

        val expense = expenses[position]

        val tvAmountCategory = view.findViewById<TextView>(R.id.tvAmountCategory)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvNotes = view.findViewById<TextView>(R.id.tvNotes)
        val tvReceipt = view.findViewById<TextView>(R.id.tvReceipt)

        tvAmountCategory.text = "R%.0f - ${expense.category}".format(expense.amount)
        tvDate.text = expense.date
        tvNotes.text = expense.notes

        if (expense.receiptUri != null) {
            tvReceipt.visibility = View.VISIBLE
        } else {
            tvReceipt.visibility = View.GONE
        }

        return view
    }
}