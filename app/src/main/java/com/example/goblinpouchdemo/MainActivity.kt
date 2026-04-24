package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.database.BudgetDbHelper
import com.example.goblinpouchdemo.databinding.ActivityMainBinding
import com.example.goblinpouchdemo.reports.CategoryTotalsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: BudgetDbHelper
    private lateinit var currentUserId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getStringExtra("USER_ID") ?: ""

        dbHelper = BudgetDbHelper(this)

        binding.btnOpenTotals.setOnClickListener {
            val intent = Intent(this, CategoryTotalsActivity::class.java)
            startActivity(intent)
        }

        binding.btnInsertSampleData.setOnClickListener {
            insertSampleData()
        }

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

    }

    private fun insertSampleData() {
        val first = dbHelper.insertExpense(
            "Groceries",
            "Bought food",
            250.0,
            "2025-11-01",
            "Food",
            ""
        )

        val second = dbHelper.insertExpense(
            "Taxi",
            "Transport to campus",
            120.0,
            "2025-11-02",
            "Transport",
            ""
        )

        val third = dbHelper.insertExpense(
            "Lunch",
            "Lunch at cafeteria",
            80.0,
            "2025-11-03",
            "Food",
            ""
        )

        val fourth = dbHelper.insertExpense(
            "Notebook",
            "Stationery",
            60.0,
            "2025-11-04",
            "School",
            ""
        )

        if (first && second && third && fourth) {
            Toast.makeText(this, "Sample expenses inserted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Could not insert sample data", Toast.LENGTH_SHORT).show()
        }
    }
}