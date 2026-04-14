package com.example.goblinpouchdemo

import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DbExample {
    private lateinit var dbRef: DatabaseReference

    //Make a data class storing the information you want to pass to the database like this
    //It's kind of like a model from asp.net mvc, storing the fields you wanna pass
    //Just make sure they have initial values like this one, even empty strings
    data class Expense(
        var name: String = "",
        var amount: Double = 0.0,
        var date: String = "",
    )

    fun saveToDatabase() {

        //path to the database. Replace "Expense" with the database needed for your feature.
        //keep the temp/ cause the database will change later once the firebase authentication is added
        //Using temp/ will stop the confusion cause each feature will have its own db folder for now
        dbRef = FirebaseDatabase.getInstance().getReference("temp/Expense")

        //Pass actual data into the data class (model thing)
        val expense = Expense("Bought Stuff", 20.00, "2023-04-01")

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

}