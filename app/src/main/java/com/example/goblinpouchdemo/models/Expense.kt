package com.example.goblinpouchdemo.models

data class Expense (
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var amount: Double = 0.0,
    var date: String = "",
    var categoryId: String = "",
    var attachment: String = ""
)