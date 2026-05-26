package com.example.goblinpouchdemo.models

data class ExpenseCategory(
    var id: String = "",
    var name: String = "",
    var monthlyBudget: Double = 0.0,
    var currentSpent: Double = 0.0
)
