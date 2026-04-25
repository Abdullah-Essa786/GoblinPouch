package com.example.goblinpouchdemo

import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.models.Category
import com.example.goblinpouchdemo.models.ExpenseCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateExpenseCategory : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/categories")

    fun createCategory(name: String, budget: Double, icon: String = "", onComplete: (Boolean) -> Unit) {

        if (currentUserId.isEmpty()){
            onComplete(false)
            return
        }

        val id = dbRef.push().key ?: return
        val category = Category(
            id = id,
            name = name,
            budgetSet = budget,
            icon = icon
        )

        dbRef.child(id).setValue(category).addOnSuccessListener{
            onComplete(true)
        }
            .addOnFailureListener {
               onComplete(false)
            }
    }
}