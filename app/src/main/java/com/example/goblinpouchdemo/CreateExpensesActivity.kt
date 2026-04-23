package com.example.goblinpouchdemo
//xml change:
//etExpenseName
//: etExpenseDescription
//: etExpenseAmount
//: etExpenseDate
//Spinner: spinnerCategoryName
//Button: btnAddExpense
//import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.goblinpouchdemo.databinding.ActivityExpensesBinding
//import com.google.firebase.database.FirebaseDatabase
//
//class CreateExpensesActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityExpensesBinding
//    private val expenseService = CreateExpenses()
//
//    private val userId = "Abdullah"
//
//    private var categoryList = mutableListOf<ExpenseCategory>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityExpensesBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        loadCategories()
//
//        binding.btnAddExpense.setOnClickListener {
//
//            val name = binding.etExpenseName.text.toString().trim()
//            val description = binding.etExpenseDescription.text.toString().trim()
//            val amountText = binding.etExpenseAmount.text.toString().trim()
//            val date = binding.etExpenseDate.text.toString().trim()
//
//            val amount = amountText.toDoubleOrNull()
//
//            if (name.isNotEmpty() &&
//                description.isNotEmpty() &&
//                amount != null &&
//                date.isNotEmpty() &&
//                categoryList.isNotEmpty()
//            ) {
//
//                val selectedCategory =
//                    categoryList[binding.spinnerCategoryName.selectedItemPosition]
//
//                expenseService.createExpense(
//                    name = name,
//                    description = description,
//                    amount = amount,
//                    date = date,
//                    categoryId = selectedCategory.id
//                )
//
//                Toast.makeText(this, "Expense created", Toast.LENGTH_SHORT).show()
//
//                clearFields()
//
//            } else {
//                Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun loadCategories() {
//
//        val dbRef = FirebaseDatabase.getInstance()
//            .getReference("temp/$userId/categories")
//
//        dbRef.get().addOnSuccessListener { snapshot ->
//
//            categoryList.clear()
//
//            for (child in snapshot.children) {
//                val category = child.getValue(ExpenseCategory::class.java)
//                if (category != null) {
//                    categoryList.add(category)
//                }
//            }
//
//            val names = categoryList.map { it.name }
//
//            val adapter = ArrayAdapter(
//                this,
//                android.R.layout.simple_spinner_item,
//                names
//            )
//
//            adapter.setDropDownViewResource(
//                android.R.layout.simple_spinner_dropdown_item
//            )
//
//            binding.spinnerCategoryName.adapter = adapter
//        }
//    }
//
//    private fun clearFields() {
//        binding.etExpenseName.text.clear()
//        binding.etExpenseDescription.text.clear()
//        binding.etExpenseAmount.text.clear()
//        binding.etExpenseDate.text.clear()
//    }
//}