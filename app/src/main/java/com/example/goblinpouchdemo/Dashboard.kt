package com.example.goblinpouchdemo

import com.example.goblinpouchdemo.NavSetup
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.goblinpouchdemo.databinding.ActivityDashboardBinding
import com.example.goblinpouchdemo.databinding.DashboardContentBinding
import com.example.goblinpouchdemo.databinding.TopNavBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Dashboard : NavSetup() {

    private lateinit var contentBinding: DashboardContentBinding
    private lateinit var dbRef : DatabaseReference
    private lateinit var currentUserId : String
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initRootBinding()
        setupCommonNav()

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""
        dbRef = FirebaseDatabase.getInstance().getReference("Users/${currentUserId}")

        navBinding.header.tvPageTitle.text = "Dashboard"

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.dashboard_content, frame, false)
        frame.addView(contentView)
        contentBinding = DashboardContentBinding.bind(contentView)

        fetchBudget()

    }

    private fun fetchBudget(){

        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                var monthlyBudget =
                    snapshot.child("profile").child("monthlyBudget").getValue(Double::class.java)
                        ?: 0.0

                val currentMonth = java.text.SimpleDateFormat(
                    "yyyy-MM",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                var totalSpent = 0.0
                var expenseSnapshot = snapshot.child("expenses")

                for (expense in expenseSnapshot.children) {
                    val amount = expense.child("amount").getValue(Double::class.java) ?: 0.0
                    val date = expense.child("date").getValue(String::class.java) ?: ""

                    if (date.startsWith(currentMonth)) {
                        totalSpent += amount
                    }
                }
                updateUi(totalSpent, monthlyBudget)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Dashboard, "Failed to load from database", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUi(totalSpent: Double, monthlyBudget: Double){
        val percentage = if (monthlyBudget > 0) {
            ((totalSpent / monthlyBudget) * 100).toInt()
        } else {
            0
        }

        contentBinding.tvBudgetAmount.text = "R%,.0f / R%,.0f".format(totalSpent, monthlyBudget)
        contentBinding.tvBudgetPercent.text = "$percentage% Used"
        contentBinding.tvDonutPercent.text = "$percentage%"

        contentBinding.progressMonthly.progress = percentage

        if (totalSpent > monthlyBudget && monthlyBudget > 0){
            contentBinding.tvBudgetAmount.setTextColor(getColor(R.color.red))
        }
        else{
            contentBinding.tvBudgetAmount.setTextColor(getColor(R.color.white))
        }
    }

}