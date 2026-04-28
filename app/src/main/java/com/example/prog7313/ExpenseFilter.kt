package com.example.prog7313

data class ExpenseFilter(
    val keyword: String = "",
    val category: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)