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

class ProfileActivity : NavSetup() {

    private lateinit var contentBinding: ActivityProfileBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var currentUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRootBinding()
        setupCommonNav()

        navBinding.header.tvPageTitle.text = "My Profile"

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_profile, frame, false)
        frame.addView(contentView)
        contentBinding = ActivityProfileBinding.bind(contentView)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Point to this user's profile node in Firebase
        dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$currentUserId/profile")

        setLoadingState(isLoading = true)
        loadProfile()
        loadExpenseStats()
        setupClickListeners()
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    private fun loadProfile() {
        // Single read — profile doesn't need a live listener since it
        // only changes when the user explicitly edits it
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setLoadingState(isLoading = false)
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        contentBinding.tvUserName.text = user.username.ifEmpty { "Not set" }
                        contentBinding.tvUserEmail.text = user.email.ifEmpty { "Not set" }

                        val personalInfo = com.example.goblinpouchdemo.databinding.ProfilePersonalInfoBinding.bind(contentBinding.layoutPersonalInfoCard.root)
                        personalInfo.tvProfileName.text = user.username.ifEmpty { "Not set" }
                        personalInfo.tvProfileAge.text = if (user.age > 0) user.age.toString() else "Not Set"
                        personalInfo.tvProfileEmail.text = user.email.ifEmpty { "Not set" }
                        personalInfo.tvProfilePhone.text = user.phone.ifEmpty { "Not set" }

                        val budgetSettings = com.example.goblinpouchdemo.databinding.ProfileBudgetSettingsBinding.bind(contentBinding.layoutBudgetCard.root)
                        budgetSettings.tvMonthlyBudget.text = "R %,.2f".format(user.monthlyBudget)
                        budgetSettings.switchBudgetAlerts.isChecked = user.budgetAlertsEnabled
                    }
                } else {
                    createDefaultProfile()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                setLoadingState(isLoading = false)
            }
        })
    }

    private fun createDefaultProfile() {
        // Writes a blank profile to Firebase so future reads always find something
        val defaultUser = User(
            username = currentUserId,
            email = "",
            phone = "",
            monthlyBudget = 0.0,
            age = 0,
            budgetAlertsEnabled = true
        )
        dbRef.setValue(defaultUser)
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    private fun loadExpenseStats() {
        // Point to the root of the user so we can see both expenses and categories nodes
        val userRootRef = FirebaseDatabase.getInstance()
            .getReference("Users/$currentUserId")

        userRootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setLoadingState(isLoading = false)
                var totalSpent = 0.0
                var transactionCount = 0

                // 1. Get the actual count of created categories
                val categoryCount = snapshot.child("categories").childrenCount.toInt()

                val currentMonth = java.text.SimpleDateFormat("yyyy-M", java.util.Locale.getDefault())
                    .format(java.util.Date())

                // 2. Loop through expenses for spending totals
                val expensesSnapshot = snapshot.child("expenses")
                for (child in expensesSnapshot.children) {
                    val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                    val date = child.child("date").getValue(String::class.java) ?: ""

                    if (date.startsWith(currentMonth)) {
                        totalSpent += amount
                    }
                    transactionCount++
                }

                // Update UI
                val stats = com.example.goblinpouchdemo.databinding.ProfileStatsRowBinding.bind(contentBinding.layoutStatsRow.root)
                stats.tvStatTotalSpent.text = "R %.0f".format(totalSpent)
                stats.tvStatTransactions.text = transactionCount.toString()

                // Now this shows how many categories EXIST, even if they have 0 expenses
                stats.tvStatCategories.text = categoryCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                setLoadingState(isLoading = false)
                Toast.makeText(this@ProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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
        contentBinding.layoutBudgetCard.btnSetMonthlyBudget.setOnClickListener {
            val intent = Intent(this, EditBudget::class.java)
            startActivity(intent)
        }
    }

    private fun setupBudgetAlertsSwitch() {
        contentBinding.layoutBudgetCard.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            // Only updates the one field in Firebase rather than rewriting the whole User object
            dbRef.child("budgetAlertsEnabled").setValue(isChecked)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save preference", Toast.LENGTH_SHORT).show()
                    // Revert the switch visually if the save failed
                    contentBinding.layoutBudgetCard.switchBudgetAlerts.isChecked = !isChecked
                }
        }
    }

    private fun setupChangePassword() {
        // Account card — needs android:id="@+id/layoutAccountCard" on its <include> tag
        contentBinding.layoutAccountCard.btnChangePassword.setOnClickListener {
            // Placeholder — wire up to Firebase Auth password reset later
            Toast.makeText(this, "Change password coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSignOut() {
        val accountCardBinding = com.example.goblinpouchdemo.databinding.ProfileAccountCardBinding.bind(contentBinding.layoutAccountCard.root)

        accountCardBinding.btnSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ -> signOut() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        // FLAG_ACTIVITY_CLEAR_TASK wipes the back stack so pressing back
        // on the login screen doesn't bring the user back into the app
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}