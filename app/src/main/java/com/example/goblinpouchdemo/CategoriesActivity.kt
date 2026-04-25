package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goblinpouchdemo.databinding.ActivityCategoriesBinding
import com.example.goblinpouchdemo.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesActivity: NavSetup() {

    private lateinit var binding: ActivityCategoriesBinding
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
        val contentView = layoutInflater.inflate(R.layout.activity_categories, frame, false)
        frame.addView(contentView)
        binding = ActivityCategoriesBinding.bind(contentView)

        navBinding.header.tvPageTitle.text = "Categories"

        setupRecyclerView()
        listenForData()

        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, CreateExpenseCategoryActivity::class.java))
        }

        binding.btnAddExpenses.setOnClickListener {
            startActivity(Intent(this, CreateExpensesActivity::class.java))
        }

    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(categories)
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun listenForData(){
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categories.clear()

                var totalMonthSpent = 0.0
                var totalMonthBudget = 0.0

                for (child in dataSnapshot.children) {
                    val category = child.getValue(Category::class.java)
                    if (category != null) {
                        categories.add(category)
                        totalMonthSpent += category.totalSpent
                        totalMonthBudget += category.budgetSet
                    }
                }

                adapter.submitList(categories)
                updateGrandTotalUI(totalMonthSpent, totalMonthBudget)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CategoriesActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGrandTotalUI(spent: Double, budget: Double) {
        binding.tvGrandTotal.text = "R %.0f / R %.0f".format(spent, budget)
        val left = budget - spent
        binding.tvBudgetLeft.text = "R %.0f".format(if (left > 0) left else 0.0)

        if(budget > 0){
            val progress = ((spent / budget) * 100).toInt()
            binding.progressGrandTotal.progress = progress
        }

    }

}