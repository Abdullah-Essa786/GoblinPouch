package com.example.goblinpouchdemo.models

data class User(
    var uid: String = "",
    var username: String = "",
    var email: String = "",
    var age: Int = 0,
    var phone: String = "",
    var monthlyBudget: Double = 0.0,
    var budgetAlertsEnabled: Boolean = true,
    var transactions : Int = 0,
    var categories : Int = 0,
    var totalSpent : Double = 0.0,
    var totalPoints: Int = 0,
    var currentAvatar: String = "",
    var unlockedAvatars: List<String> = emptyList(),
    var joinDate: String = ""
)
