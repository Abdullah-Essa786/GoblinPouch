package com.example.goblinpouchdemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goblinpouchdemo.databinding.ActivityEditBudgetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditBudget : NavSetup() {

    private lateinit var contentBinding: ActivityEditBudgetBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/profile")

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_edit_budget, frame, false)
        frame.addView(contentView)
        contentBinding = ActivityEditBudgetBinding.bind(contentView)

        navBinding.header.tvPageTitle.text = "Edit Budget"

        dbRef.child("monthlyBudget").get().addOnSuccessListener {
            val currentBudget = it.getValue(Double::class.java) ?: 0.0
            if (currentBudget > 0){
                contentBinding.etBudgetInput.setText(currentBudget.toString())
            }
        }

        contentBinding.btnSaveBudget.setOnClickListener {
            val amount = contentBinding.etBudgetInput.text.toString().toDoubleOrNull()

            if (amount != null && amount >= 0) {
                dbRef.child("monthlyBudget").setValue(amount).addOnSuccessListener {
                    Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else{
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

    }
}