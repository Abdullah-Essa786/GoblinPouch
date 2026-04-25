package com.example.goblinpouchdemo

import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateExpenses {

    private lateinit var dbRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser ?: ""

    fun createExpense(
        name: String,
        description: String,
        amount: Double,
        date: String,
        categoryId: String
    ) {
        dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/expenses")

        val id = dbRef.push().key ?: return

        val expense = Expense(
            id = id,
            name = name,
            description = description,
            amount = amount,
            date = date,
            category = categoryId,
            attachment = ""   // keeping the attachment empty for now
        )

        dbRef.child(id).setValue(expense)
            .addOnSuccessListener {
                println("Expense successfully created")
            }
            .addOnFailureListener {
                println("Failed to create expense")
            }

    }
}