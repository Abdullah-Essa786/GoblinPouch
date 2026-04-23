package com.example.goblinpouchdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goblinpouchdemo.databinding.ActivityTransactionHistoryBinding
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var adapter: ExpenseAdapter
    private val currentUserId = "Abdullah"

    // SimpleDateFormat parses/formats dates — must match the format used in AddExpenseActivity
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadLast30Days()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(mutableListOf())

        // tapping a transaction opens its receipt if one exists
        adapter.onItemClick = { expense ->
            if (expense.attachment != "None" && expense.attachment.isNotEmpty()) {
                val intent = android.content.Intent(this, ViewReceipt::class.java)
                intent.putExtra("EXPENSE_ID", expense.id)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No receipt attached", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun loadLast30Days() {
        // Calendar.getInstance() gives us today's date
        val calendar = Calendar.getInstance()
        val endDate = calendar.time  // today

        // subtract 30 days to get our start date
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.time  // 30 days ago

        // show the date range in the label at the top of the screen
        binding.tvTimelineLabel.text =
            "Timeline: ${dateFormat.format(startDate)} → ${dateFormat.format(endDate)}"

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("temp/$currentUserId/Expense")

        // watch Firebase for changes — list updates automatically if new expenses are added
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filtered = mutableListOf<Expense>()
                var totalSpent = 0.0

                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java) ?: continue
                    expense.id = child.key ?: ""

                    // try to convert the expense's date string into an actual Date object
                    val expenseDate = try {
                        dateFormat.parse(expense.date)
                    } catch (e: Exception) {
                        null  // if the date string is invalid, skip this expense
                    }

                    // only include expenses that fall within the last 30 days
                    if (expenseDate != null &&
                        !expenseDate.before(startDate) &&
                        !expenseDate.after(endDate)) {
                        filtered.add(expense)
                        totalSpent += expense.amount
                    }
                }

                // sort so the most recent expenses appear at the top
                filtered.sortByDescending { it.date }
                adapter.submitList(filtered)

                // update the summary card numbers
                binding.layoutSummaryCard.tvTotalSpending.text = "R %.2f".format(totalSpent)
                binding.layoutSummaryCard.tvTransactionCount.text = filtered.size.toString()

                // groupBy groups expenses by category, then we find whichever category
                // has the highest total — that's the most expensive one
                val mostExpensive = filtered
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .maxByOrNull { it.value }

                binding.layoutSummaryCard.tvMostExpensiveCategory.text =
                    mostExpensive?.key ?: "—"  // "—" if there are no expenses
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@TransactionHistoryActivity,
                    "Failed to load: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}