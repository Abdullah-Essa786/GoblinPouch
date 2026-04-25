//For referencing purposes or if you wanna see more on how to do database stuff
//All the information was taken from either class stuff (The quiz app or camera app)
// Or the firebase documentation (https://firebase.google.com/docs/database/android/read-and-write)

package com.example.goblinpouchdemo

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.goblinpouchdemo.models.Expense

public class DbExample {
    private lateinit var dbRef: DatabaseReference

    public fun saveToDatabase() {

        //path to the database. Replace "Expense" with the database needed for your feature.
        //And name with your name
        //keep the temp/ cause the database will change later once the firebase authentication is added
        //Using temp/Name will stop the confusion cause each feature will have its own db folder for now
        dbRef = FirebaseDatabase.getInstance().getReference("temp/Abdullah/Expense")

        //Pass actual data into the data class (model thing)
        val expense = Expense("", "Bought some stuff cause why not",
            20.00, "2023-04-01", "Stuff", "None")

        //set value is what gets sent to the database.
        // Essentially pass in the object of the data class
        dbRef.push().setValue(expense)
            .addOnSuccessListener {
                //Can just add like a Toast message to show it saved successfully
            }
            .addOnFailureListener {
                //Can just add like a Toast message to show it failed to save
            }
    }

    fun readFromDatabase(){

        //get the reference to the database
        dbRef = FirebaseDatabase.getInstance().getReference("temp/Name/Expense")

        //Single reads (i.e. Reading data one time), so like if you wanna load data one time
        //when the app opens, like user data, you can use this
        //snapshot is just like a copy of the database at the time of the read
        dbRef.get().addOnSuccessListener{ snapshot ->
            if (snapshot.exists()){
                for (child in snapshot.children){
                    val expense = child.getValue(Expense::class.java)
                    //Do something with the data, like print it out
                    println(expense?.description)
                }
            }
        }.addOnFailureListener {
            //handle error
        }

        //For continuous read (like when a new expense is added, getting total for categories, etc.
        //Use a Value Event Listener like this
        val expenseListener = object : ValueEventListener {
            //onDataChange is called when the data changes in the database
            override fun onDataChange(snapshot: DataSnapshot) {
                //Create a list of expense objects
                val expenseList = mutableListOf<Expense>()

                //Populate the list with the data from the database
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    expense?.let { expenseList.add(it) }
                }
                //After this, you now have a list with all data you needed from the database
                //Do something with the data here, like print it out
                for (expense in expenseList) {
                    println(expense)
                }

            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        //add the expense listener to the database reference
        dbRef.addValueEventListener(expenseListener)

    }


}