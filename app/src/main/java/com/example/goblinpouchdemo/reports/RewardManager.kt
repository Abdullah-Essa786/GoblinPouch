package com.example.goblinpouchdemo.reports

import com.example.goblinpouchdemo.models.Expense
import com.example.goblinpouchdemo.models.ExpenseCategory
import com.google.firebase.database.FirebaseDatabase

class RewardManager {

    private val userId = "Abdullah"

    fun calculateRewards(
        categories: List<ExpenseCategory>,
        expenses: List<Expense>
    ) {

        var totalPoints = 0

        for (category in categories) {

            val totalSpent = expenses
                .filter { it.categoryId == category.id }
                .sumOf { it.amount }

            if (totalSpent <= category.monthlyBudget) {
                totalPoints += 50
            }

            if (totalSpent <= category.monthlyBudget * 0.8) {
                totalPoints += 50
            }
        }

        FirebaseDatabase.getInstance()
            .getReference("temp/$userId/rewards/points")
            .setValue(totalPoints)
    }
}