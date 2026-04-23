package com.example.goblinpouchdemo.reports

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.R
import com.example.goblinpouchdemo.database.BudgetDbHelper
import java.text.NumberFormat
import java.util.Locale

class CategoryTotalsActivity : AppCompatActivity() {

    private lateinit var dbHelper: BudgetDbHelper
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnLoadTotals: Button
    private lateinit var tvGrandTotal: TextView
    private lateinit var listViewTotals: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_totals)

        dbHelper = BudgetDbHelper(this)

        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnLoadTotals = findViewById(R.id.btnLoadTotals)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)
        listViewTotals = findViewById(R.id.listViewTotals)

        btnLoadTotals.setOnClickListener {
            loadCategoryTotals()
        }
    }

    private fun loadCategoryTotals() {
        val startDate = etStartDate.text.toString().trim()
        val endDate = etEndDate.text.toString().trim()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please enter both dates", Toast.LENGTH_SHORT).show()
            return
        }

        val totals = dbHelper.getCategoryTotals(startDate, endDate)

        if (totals.isEmpty()) {
            tvGrandTotal.text = "Grand Total: R0.00"
            listViewTotals.adapter = null
            Toast.makeText(this, "No expenses found for this period", Toast.LENGTH_SHORT).show()
            return
        }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        val displayList = mutableListOf<String>()
        var grandTotal = 0.0

        for (item in totals) {
            displayList.add(item.category + " - " + currencyFormat.format(item.totalAmount))
            grandTotal += item.totalAmount
        }

        tvGrandTotal.text = "Grand Total: " + currencyFormat.format(grandTotal)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            displayList
        )

        listViewTotals.adapter = adapter
    }
}