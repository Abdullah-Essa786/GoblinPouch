package com.example.goblinpouchdemo.reports

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.CategoryAdapter
import com.example.goblinpouchdemo.NavSetup
import com.example.goblinpouchdemo.R
import com.example.goblinpouchdemo.database.BudgetDbHelper
import com.example.goblinpouchdemo.databinding.ActivityCategoryTotalsBinding
import com.example.goblinpouchdemo.databinding.TopNavBinding
import com.example.goblinpouchdemo.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class CategoryTotalsActivity : NavSetup() {

    private lateinit var binding: ActivityCategoryTotalsBinding
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/categories")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_category_totals, frame, false)
        frame.addView(contentView)
        binding = ActivityCategoryTotalsBinding.bind(contentView)

        navBinding.header.tvPageTitle.text = "Category Totals"
        setupDatePickers()
        binding.etStartDate.isFocusable = false
        binding.etEndDate.isFocusable = false

        binding.btnLoadTotals.setOnClickListener {
            loadCategoryTotals()
        }

    }

    private fun loadCategoryTotals() {
        val startDate = binding.etStartDate.text.toString().trim()
        val endDate = binding.etEndDate.text.toString().trim()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/expenses")

        expenseRef.get().addOnSuccessListener { snapshot ->

            val categoryMap = mutableMapOf<String, Double>()
            var grandTotal = 0.0
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

            for (child in snapshot.children) {
                val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                val categoryName = child.child("category").getValue(String::class.java) ?: "Other"
                val date = child.child("date").getValue(String::class.java) ?: ""

                // String-based range comparison (yyyy-MM-dd)
                if (date in startDate..endDate) {
                    categoryMap[categoryName] = (categoryMap[categoryName] ?: 0.0) + amount
                    grandTotal += amount
                }
            }

            val displayList = categoryMap.map { (name, total) ->
                "$name - ${currencyFormat.format(total)}"
            }

            // Update UI
            binding.tvGrandTotal.text = "Grand Total: ${currencyFormat.format(grandTotal)}"

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                displayList
            )
            binding.listViewTotals.adapter = adapter

            if (displayList.isEmpty()) {
                Toast.makeText(this, "No expenses found for this range", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDatePickers() {
        val dateSetListener = { editText: EditText ->
            val cal = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                // month + 1 because Calendar months are 0-indexed
                val date = "%d-%d-%d".format(year, month + 1, day)
                editText.setText(date)
            },
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etStartDate.setOnClickListener { dateSetListener(binding.etStartDate) }
        binding.etEndDate.setOnClickListener { dateSetListener(binding.etEndDate) }
    }

}