package com.example.goblinpouchdemo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import com.example.goblinpouchdemo.databinding.ActivityAddCategoryBinding

class CreateExpenseCategoryActivity : NavSetup() {
    private lateinit var contentBinding: ActivityAddCategoryBinding
    private val categoryService = CreateExpenseCategory()
    private var selectedIconName = "ic_grocery"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_add_category, frame, false)
        frame.addView(contentView)
        contentBinding = ActivityAddCategoryBinding.bind(contentView)

        navBinding.header.tvPageTitle.text = "Add Category"

        setupSpinners()
        setupIconSelectors()

        contentBinding.btnAddCategory.setOnClickListener {

            val name = contentBinding.spinnerCategoryName.selectedItem?.toString() ?: ""
            val budget = contentBinding.etBudgetAmount.text.toString().toDoubleOrNull()

            if (name.isEmpty() || budget == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            categoryService.createCategory(name, budget, selectedIconName){ success ->
                if (success){
                    Toast.makeText(this, "Category created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else{
                    Toast.makeText(this, "Failed to create category", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun setupSpinners(){
        val categories = arrayOf("Haircut", "Luxury", "Snacks", "Dinners", "Going Out", "Petrol", "Groceries")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        contentBinding.spinnerCategoryName.adapter = adapter

        val frequencies = arrayOf("Monthly", "Weekly", "Daily")
        contentBinding.spinnerFrequency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
    }

    private fun setupIconSelectors(){
        val icons = mapOf(
            contentBinding.iconGroceries to "ic_grocery",
            contentBinding.iconPetrol to "ic_petrol",
            contentBinding.iconSnacks to "ic_snacks",
            contentBinding.iconDinners to "ic_dinners",
            contentBinding.iconGoingOut to "ic_going_out",
            contentBinding.iconHaircut to "ic_haircut",
            contentBinding.iconLuxury to "ic_luxury"
        )

        icons.forEach { (button, name) ->
            button.setOnClickListener {
                selectedIconName = name
                resetIconSelection(icons.keys)
                button.alpha = 0.5f
            }
        }

    }

    private fun resetIconSelection(buttons: Set<ImageButton>) {
        buttons.forEach {
            it.alpha = 1.0f
        }
    }

}