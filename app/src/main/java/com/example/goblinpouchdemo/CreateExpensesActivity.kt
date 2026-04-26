package com.example.goblinpouchdemo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.goblinpouchdemo.databinding.ActivityAddExpenseBinding
import com.example.goblinpouchdemo.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class CreateExpensesActivity : NavSetup() {

    private lateinit var contentBinding: ActivityAddExpenseBinding
    private val expenseService = CreateExpenses()
    private lateinit var userId : String
    private var categoryList = mutableListOf<Category>()
    private lateinit var auth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        dbRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/expenses")

        val id = dbRef.push().key

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_add_expense, frame, false)
        frame.addView(contentView)
        contentBinding = ActivityAddExpenseBinding.bind(contentView)

        navBinding.header.tvPageTitle.text = "Create Expense"

        loadCategories()
        setUpDatePicker()

        contentBinding.btnAttachPhoto.setOnClickListener {
            val intent = Intent(this, ReceiptCapture::class.java)
            intent.putExtra("EXPENSE_ID", id)
            startActivity(intent)
        }

        contentBinding.btnSaveExpense.setOnClickListener {

            val description = contentBinding.etNotes.text.toString().trim()
            val amountText = contentBinding.etExpenseAmount.text.toString().trim()
            val date = contentBinding.tvExpenseDate.text.toString().trim()

            val amount = amountText.toDoubleOrNull()

            if (description.isEmpty() ||
                amount == null ||
                date == "Select Date" ||
                categoryList.isEmpty()
            ) {
                Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategory = categoryList[contentBinding.spCategory.selectedItemPosition]

            expenseService.createExpense(
                id = id ?: "",
                description = description,
                amount = amount,
                date = date,
                categoryId = selectedCategory.name
            ){success ->
                if (success){
                    Toast.makeText(this, "Expense created", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else{
                    Toast.makeText(this, "Failed to create expense", Toast.LENGTH_SHORT).show()
                }
            }
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
                val category = child.getValue(Category::class.java)
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

            contentBinding.spCategory.adapter = adapter
        }
    }

    private fun setUpDatePicker(){
        contentBinding.tvExpenseDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val dateString = "$year-${month + 1}-$day"
                contentBinding.tvExpenseDate.text = dateString
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}