package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.goblinpouchdemo.databinding.ActivityExpensesBinding
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpensesActivity : AppCompatActivity() {

    // binding lets us access XML views without findViewById
    private lateinit var binding: ActivityExpensesBinding

    // dbRef is our connection to the Firebase database
    private lateinit var dbRef: DatabaseReference

    // adapter feeds expense data into the RecyclerView list
    private lateinit var adapter: ExpenseAdapter

    // hardcoded user ID — will come from Firebase Auth later
    private val currentUserId = "Abdullah"

    // we store the listener here so we can turn it off when the screen closes
    private lateinit var expenseListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate = "build the screen from the XML file"
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // point to this user's expenses folder in Firebase
        dbRef = FirebaseDatabase.getInstance()
            .getReference("temp/$currentUserId/Expense")

        setupRecyclerView()
        listenForExpenses()

        // when the Add Expense button is tapped, open AddExpenseActivity
        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(this, CreateExpensesActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        // create the adapter with an empty list to start
        adapter = ExpenseAdapter(mutableListOf())

        // what happens when the user taps an expense card
        adapter.onItemClick = { expense ->
            if (expense.attachment != "None" && expense.attachment.isNotEmpty()) {
                // if the expense has a receipt photo, open the receipt viewer
                val intent = Intent(this, ViewReceipt::class.java)
                intent.putExtra("EXPENSE_ID", expense.id)
                startActivity(intent)
            } else {
                // otherwise just tell the user there's no receipt
                Toast.makeText(this, "No receipt attached", Toast.LENGTH_SHORT).show()
            }
        }

        // LinearLayoutManager = stack items in a vertical list
//        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
//        binding.rvExpenses.adapter = adapter
    }

    private fun listenForExpenses() {
        // ValueEventListener keeps watching Firebase and fires every time data changes
        // so the list updates automatically when expenses are added or removed
        expenseListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot = a copy of all the expense data in Firebase right now
                val expenseList = mutableListOf<Expense>()

                // loop through every expense in the database
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    if (expense != null) {
                        // child.key is the unique ID Firebase gave this expense (e.g. "-Oqui7tr...")
                        expense.id = child.key ?: ""
                        expenseList.add(expense)
                    }
                }

                // hand the full list to the adapter so it can display it
                adapter.submitList(expenseList)
            }

            override fun onCancelled(error: DatabaseError) {
                // something went wrong reading from Firebase — show the error
                Toast.makeText(
                    this@ExpensesActivity,
                    "Failed to load: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // attach the listener to our database path so it starts watching
        dbRef.addValueEventListener(expenseListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // remove the listener when the screen is closed
        // without this it would keep running in the background and waste resources
        dbRef.removeEventListener(expenseListener)
    }
}