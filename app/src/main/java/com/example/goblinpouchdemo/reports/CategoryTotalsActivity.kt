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


    }

    private fun loadCategoryTotals() {
        val startDate = binding.etStartDate.text.toString().trim()
        val endDate = binding.etEndDate.text.toString().trim()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please enter both dates", Toast.LENGTH_SHORT).show()
            return
        }

        dbRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                binding.tvGrandTotal.text = "Grand Total: R0.00"
                binding.listViewTotals.adapter = null
                Toast.makeText(this, "No Categories found", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }


            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            val displayList = mutableListOf<String>()
            var grandTotal = 0.0

            for (child in snapshot.children) {
                val category = child.getValue(Category::class.java)
                if (category != null) {
                    displayList.add("${category.name} - ${currencyFormat.format(category.totalSpent)}")
                    grandTotal += category.totalSpent
                }
            }

            // Update UI with calculated totals
            binding.tvGrandTotal.text = "Grand Total: " + currencyFormat.format(grandTotal)

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                displayList
            )
            binding.listViewTotals.adapter = adapter

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch data from cloud", Toast.LENGTH_SHORT).show()
        }
    }
}