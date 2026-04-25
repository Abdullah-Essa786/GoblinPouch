package com.example.goblinpouchdemo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.databinding.ActivityExpensesBinding
import com.example.goblinpouchdemo.models.ExpenseCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateExpensesActivity : NavSetup() {

    private lateinit var contentBinding: ActivityExpensesBinding
    private val expenseService = CreateExpenses()
    private lateinit var userId : String
    private var categoryList = mutableListOf<ExpenseCategory>()
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_expenses, frame, false)
        frame.addView(contentView)
        contentBinding = ActivityExpensesBinding.bind(contentView)

        loadCategories()

        contentBinding.btnAddExpense.setOnClickListener {

            val name = contentBinding.etExpenseName.text.toString().trim()
            val description = contentBinding.etExpenseDescription.text.toString().trim()
            val amountText = contentBinding.etExpenseAmount.text.toString().trim()
            val date = contentBinding.etExpenseDate.text.toString().trim()

            val amount = amountText.toDoubleOrNull()

            if (name.isEmpty() ||
                description.isEmpty() ||
                amount == null ||
                date.isEmpty() ||
                categoryList.isEmpty()
            ) {
                Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategory = categoryList[contentBinding.spinnerCategoryName.selectedItemPosition]

            expenseService.createExpense(
                name = name,
                description = description,
                amount = amount,
                date = date,
                categoryId = selectedCategory.id   // FIXED
            )

            Toast.makeText(this, "Expense created", Toast.LENGTH_SHORT).show()

            clearFields()
        }
    }

    private fun loadCategories() {
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/categories")

        dbRef.get().addOnSuccessListener { snapshot ->

            categoryList.clear()

            for (child in snapshot.children) {
                val category = child.getValue(ExpenseCategory::class.java)
                category?.let { categoryList.add(it) }
            }

            val names = categoryList.map { it.name }

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                names
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            contentBinding.spinnerCategoryName.adapter = adapter
        }
    }

    private fun clearFields() {
        contentBinding.etExpenseName.text.clear()
        contentBinding.etExpenseDescription.text.clear()
        contentBinding.etExpenseAmount.text.clear()
        contentBinding.etExpenseDate.text.clear()
    }
}