package com.example.goblinpouchdemo
// for the xml change:
//android:id="@+id/etCategoryName"
//    android:layout_width="match_parent"
//    android:layout_height="52dp"
//    android:hint="Enter category name"
//    android:background="@drawable/bg_card"
//    android:padding="14dp"
//    android:textColor="#FFFFFF"/>
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.goblinpouchdemo.databinding.ActivityAddCategoryBinding
//
//class CreateExpenseCategoryActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAddCategoryBinding
//    private val categoryService = CreateExpenseCategory()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.btnAddCategory.setOnClickListener {
//
//            val name = binding.etCategoryName.text.toString().trim()
//
//            if (name.isNotEmpty()) {
//
//                categoryService.createCategory(name)
//
//                Toast.makeText(this, "Category created", Toast.LENGTH_SHORT).show()
//
//                binding.etCategoryName.text.clear()
//
//            } else {
//                Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}