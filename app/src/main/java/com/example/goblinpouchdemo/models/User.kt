package com.example.goblinpouchdemo.models

data class User(
    var uid: String = "",
    var username: String = "",
    var email: String = "",
    var phone: String = "",
    var monthlyBudget: Double = 0.0,
    var budgetAlertsEnabled: Boolean = true,
    var totalPoints: Int = 0,
    var currentAvatar: String = "",
    var unlockedAvatars: List<String> = emptyList(),
    var joinDate: String = ""
)
