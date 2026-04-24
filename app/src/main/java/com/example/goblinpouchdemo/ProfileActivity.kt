package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.databinding.ActivityProfileBinding
import com.example.goblinpouchdemo.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbRef: DatabaseReference

    // Hardcoded for now — replace with FirebaseAuth.getInstance().currentUser?.uid later
    private lateinit var currentUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getStringExtra("USER_ID") ?: ""

        // Point to this user's profile node in Firebase
        dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$currentUserId")

        loadProfile()
        loadExpenseStats()
        setupClickListeners()
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    private fun loadProfile() {
        // Single read — profile doesn't need a live listener since it
        // only changes when the user explicitly edits it
        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    // ⚠️ These two lines will crash if activity_profile.xml doesn't have
                    // tvUserName and tvUserEmail in a header section — remove if not present
                    binding.tvUserName.text = user.username.ifEmpty { "Not set" }
                    binding.tvUserEmail.text = user.email.ifEmpty { "Not set" }

                    // Personal info card — needs android:id="@+id/layoutPersonalInfoCard"
                    // on its <include> tag in activity_profile.xml
                    val personalInfo = binding.layoutPersonalInfoCard
                    personalInfo.tvProfileName.text = user.username.ifEmpty { "Not set" }
                    personalInfo.tvProfileAge.text = user.age.toString().ifEmpty { "Not Set" }
                    personalInfo.tvProfileEmail.text = user.email.ifEmpty { "Not set" }
                    personalInfo.tvProfilePhone.text = user.phone.ifEmpty { "Not set" }

                    // Budget card — needs android:id="@+id/layoutBudgetCard"
                    val budgetSettings = binding.layoutBudgetCard
                    budgetSettings.tvMonthlyBudget.text = "R %.2f".format(user.monthlyBudget)
                    budgetSettings.switchBudgetAlerts.isChecked = user.budgetAlertsEnabled
                }
            } else {
                // First time this user opens the profile — write defaults to Firebase
                createDefaultProfile()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDefaultProfile() {
        // Writes a blank profile to Firebase so future reads always find something
        val defaultUser = User(
            username = currentUserId,
            email = "",
            phone = "",
            monthlyBudget = 0.0,
            budgetAlertsEnabled = true
        )
        dbRef.setValue(defaultUser)
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    private fun loadExpenseStats() {
        val expenseRef = FirebaseDatabase.getInstance()
            .getReference("temp/$currentUserId/Expense")

        // Continuous listener — stats update automatically when expenses are added/removed
        expenseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSpent = 0.0
                val categories = mutableSetOf<String>() // Set auto-deduplicates categories
                var transactionCount = 0

                // Loop through every expense and accumulate totals
                for (child in snapshot.children) {
                    val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                    val category = child.child("category").getValue(String::class.java) ?: ""

                    totalSpent += amount
                    transactionCount++
                    if (category.isNotEmpty()) categories.add(category)
                }

                // Stats row — needs android:id="@+id/layoutStatsRow" on its <include> tag
                val stats = binding.layoutStatsRow
                stats.tvStatTotalSpent.text = "R %.2f".format(totalSpent)
                stats.tvStatTransactions.text = transactionCount.toString()
                stats.tvStatCategories.text = categories.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load stats: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // ─── Click listeners ──────────────────────────────────────────────────────

    private fun setupClickListeners() {
        setupBudgetRow()
        setupBudgetAlertsSwitch()
        setupChangePassword()
        setupSignOut()
    }

    private fun setupBudgetRow() {
        // Tapping the budget row opens a dialog to enter a new amount
        binding.layoutBudgetCard.btnSetMonthlyBudget.setOnClickListener {
            val input = android.widget.EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                hint = "Enter amount in Rands"
            }

            AlertDialog.Builder(this)
                .setTitle("Set Monthly Budget")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newBudget = input.text.toString().toDoubleOrNull()
                    if (newBudget != null && newBudget >= 0) {
                        saveBudget(newBudget)
                    } else {
                        Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun saveBudget(amount: Double) {
        // Only updates the monthlyBudget field — doesn't overwrite the whole profile object
        dbRef.child("monthlyBudget").setValue(amount)
            .addOnSuccessListener {
                // Update the UI immediately so user doesn't have to reload
                binding.layoutBudgetCard.tvMonthlyBudget.text = "R %.2f".format(amount)
                Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update budget", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBudgetAlertsSwitch() {
        binding.layoutBudgetCard.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            // Only updates the one field in Firebase rather than rewriting the whole User object
            dbRef.child("budgetAlertsEnabled").setValue(isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save preference", Toast.LENGTH_SHORT).show()
                    // Revert the switch visually if the save failed
                    binding.layoutBudgetCard.switchBudgetAlerts.isChecked = !isChecked
                }
        }
    }

    private fun setupChangePassword() {
        // Account card — needs android:id="@+id/layoutAccountCard" on its <include> tag
        binding.layoutAccountCard.btnChangePassword.setOnClickListener {
            // Placeholder — wire up to Firebase Auth password reset later
            Toast.makeText(this, "Change password coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSignOut() {
        binding.layoutAccountCard.btnSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ -> signOut() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun signOut() {
        // FLAG_ACTIVITY_CLEAR_TASK wipes the back stack so pressing back
        // on the login screen doesn't bring the user back into the app
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}