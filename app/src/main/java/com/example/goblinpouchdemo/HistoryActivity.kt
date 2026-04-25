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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val currentUserId = "Abdullah"

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var startDate: Date? = null
    private var endDate: Date? = null

    // store the active listener and database reference so we can remove the listener later
    private var expenseListener: ValueEventListener? = null
    private var activeDbRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        setupDateSelection()

        // load the current month by default when the screen first opens
        setDefaultDates()
        loadFilteredHistory()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(mutableListOf())
        binding.rvHistoryCategories.layoutManager = LinearLayoutManager(this)
        binding.rvHistoryCategories.adapter = categoryAdapter
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabMonth, binding.tabWeek, binding.tabDay)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                // reset all tabs back to unselected style first
                tabs.forEach {
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
        binding.layoutDateSelection.setOnClickListener {
            showDatePicker { date ->
                startDate = date

                // once start is picked, immediately open the end date picker
                showDatePicker { end ->
                    endDate = end
                    updateDateRangeDisplay()
                    loadFilteredHistory()
                }
            }
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

        // remove the previous listener before attaching a new one
        // without this, every tab tap or date change stacks up another listener
        expenseListener?.let { activeDbRef?.removeEventListener(it) }

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("temp/$currentUserId/Expense")

        // save the reference so we can remove the listener later
        activeDbRef = dbRef

        // create the listener and save it to our property
        expenseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filtered = mutableListOf<Expense>()

                // loop through every expense and keep only ones in the selected range
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java) ?: continue

                    // parse the stored date string into a Date so we can compare it
                    val expenseDate = try {
                        dbDateFormat.parse(expense.date)
                    } catch (e: Exception) {
                        null  // skip this expense if the date string can't be parsed
                    }

                    // before(start) = too old, after(end) = too new — we want everything in between
                    if (expenseDate != null &&
                        !expenseDate.before(start) &&
                        !expenseDate.after(end)) {
                        filtered.add(expense)
                    }
                }

                // groupBy bundles all expenses with the same category together
                // then we calculate a total and count for each group
                val categoryGroups = filtered
                    .groupBy { it.category }
                    .map { (categoryName, expenses) ->
                        Category(
                            name = categoryName,
                            totalSpent = expenses.sumOf { it.amount }
                        )
                    }
                    .sortedByDescending { it.totalSpent }  // highest spending category first

                categoryAdapter.submitList(categoryGroups)

                // only show the graph area if there's actually data to display
                binding.graphContainer.visibility =
                    if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // attach the saved listener — now exactly one listener is active at a time
        dbRef.addValueEventListener(expenseListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        // remove the listener when the screen closes so nothing runs in the background
        expenseListener?.let { activeDbRef?.removeEventListener(it) }
    }
}