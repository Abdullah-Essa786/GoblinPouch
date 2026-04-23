package com.example.goblinpouchdemo

import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.models.ExpenseCategory
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateExpenseCategory : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private val userId = "Abdullah"

    fun createCategory(name: String) {

        dbRef = FirebaseDatabase.getInstance().getReference("temp/$userId/categories")

        val id = dbRef.push().key!!

        val category = ExpenseCategory(
            id = id,
            name = name
        )

        dbRef.child(id).setValue(category).addOnSuccessListener{
            println("Category Successfully Created!")
        }
            .addOnFailureListener {
                println("Failed To Create Category")
            }
    }
}