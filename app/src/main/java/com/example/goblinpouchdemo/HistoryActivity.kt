package com.example.goblinpouchdemo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goblinpouchdemo.databinding.ActivityHistoryBinding
import com.example.goblinpouchdemo.models.Category
import com.example.goblinpouchdemo.models.CategorySummary
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : NavSetup() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: ExpenseAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var currentUserId : String

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("yyy-M-d", Locale.getDefault())

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initRootBinding()
        setupCommonNav()

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.activity_history, frame, false)
        frame.addView(contentView)
        binding = ActivityHistoryBinding.bind(contentView)
        navBinding.header.tvPageTitle.text = "Transaction History"

        setupRecyclerView()
        setupTabs()
        setupDateSelection()

        // load the current month by default when the screen first opens
        setDefaultDates()
        loadFilteredHistory()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(mutableListOf())
        binding.rvHistoryTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvHistoryTransactions.adapter = adapter
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabMonth, binding.tabWeek, binding.tabDay)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                // reset all tabs back to unselected style first
                tabs.forEach { it ->
                    it.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                    it.background = null
                }

                // then highlight just the tapped tab
                tab.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                tab.background = ContextCompat.getDrawable(this, R.drawable.bg_btn)

                // update the date range and reload data for whichever tab was tapped
                adjustRangeByTab(tab.id)
            }
        }
    }

    private fun adjustRangeByTab(tabId: Int) {
        val cal = Calendar.getInstance()

        // end date is always today
        endDate = cal.time

        // roll back the start date depending on which tab was tapped
        when (tabId) {
            R.id.tabMonth -> cal.add(Calendar.MONTH, -1)        // 1 month ago
            R.id.tabWeek  -> cal.add(Calendar.DAY_OF_YEAR, -7)  // 7 days ago
            R.id.tabDay   -> cal.add(Calendar.DAY_OF_YEAR, -1)  // yesterday
        }

        startDate = cal.time

        // update the date label and fetch matching expenses
        updateDateRangeDisplay()
        loadFilteredHistory()
    }

    private fun setupDateSelection() {
        // tapping the date area opens a custom range picker
        // first picker = start date, second picker = end date
        binding.tvDateRange.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                cal.set(year, month, day)
                startDate = cal.time
                endDate = Calendar.getInstance().time
                updateDateRangeDisplay()
                loadFilteredHistory()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setDefaultDates() {
        val cal = Calendar.getInstance()

        // end = today
        endDate = cal.time

        // start = the 1st of the current month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = cal.time

        updateDateRangeDisplay()
    }

    private fun updateDateRangeDisplay() {
        val start = startDate ?: return
        val end = endDate ?: return
        binding.tvDateRange.text = "${dateFormat.format(start)} → ${dateFormat.format(end)}"
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day)
            onDateSelected(cal.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadFilteredHistory() {
        val start = startDate ?: return
        val end = endDate ?: return

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""
        dbRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/expenses")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<Expense>()
                var totalSpent = 0.0

                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java) ?: continue
                    expense.id = child.key ?: ""

                    val expenseDate = try {
                        dbDateFormat.parse(expense.date)
                    } catch (e: Exception) {
                        null
                    }
                    if (expenseDate != null && !expenseDate.before(start) && !expenseDate.after(end)) {
                        filteredList.add(expense)
                        totalSpent += expense.amount
                    }
                }

                filteredList.sortedByDescending { it.date }
                adapter.submitList(filteredList)

                binding.layoutSummaryCard.tvTotalSpending.text = "R %.2f".format(totalSpent)
                binding.layoutSummaryCard.tvTransactionCount.text = filteredList.size.toString()

                val topCat = filteredList.groupBy { it.category }
                    .maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "None"

                binding.layoutSummaryCard.tvMostExpensiveCategory.text = topCat

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}