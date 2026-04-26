package com.example.goblinpouchdemo

import android.widget.Toast
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateExpenses {

    private lateinit var dbRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    fun createExpense(
        id: String,
        description: String,
        amount: Double,
        date: String,
        categoryId: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (userId.isEmpty()) return onComplete(false)

        dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/expenses")


        val expenseRef = FirebaseDatabase.getInstance()
            .getReference("Users/${userId}/expenses/${id}")

        expenseRef.child("attachment").get().addOnSuccessListener { snapshot ->
            // If snapshot exists, use it; otherwise, default to empty string
            val existingAttachment = snapshot.getValue(String::class.java) ?: ""

            val expense = Expense(
                id = id,
                description = description,
                amount = amount,
                date = date,
                category = categoryId,
                attachment = existingAttachment // 2. Preserve the image!
            )

            // 3. Now save the full object
            expenseRef.setValue(expense)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }

        }.addOnFailureListener {
            onComplete(false)
        }

    }
}