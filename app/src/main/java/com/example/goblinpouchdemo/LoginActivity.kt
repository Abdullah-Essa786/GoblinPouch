package com.example.goblinpouchdemo

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity(){
    lateinit var emailInput: EditText
    lateinit var passwordInput: EditText
    lateinit var binding : LoginActivity
    private lateinit var auth: FirebaseAuth


    //Will check if email is found first then if password is correct from db or smth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth;
        val currentUser = Firebase.auth.currentUser

        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        //this may be redundent might edit later

        val txtRegister = findViewById<TextView>(R.id.txtRegister)
        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
//checks if email exists and if the edt boxes are filled when btn is clicked
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
// calls loop check to see if it exists else it empties the edtboxes and shows popup
            if (!loopCheckEmail()) {
                emailInput.setText("")
                passwordInput.setText("")
                popUpMenu()
            } else {
                // Email found — add navigation here
                passChecksToSignIn(email,password)
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            }
        }

    }
    //Creates pop up if password is wrong

    fun popUpMenu(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please Register")
        builder.setMessage("Password not Found.\nPlease Register to continue")
        builder.setPositiveButton("Register") { dialog, _ -> dialog.dismiss() }
        builder.create().show()

    }

    //looping through all emails in db to see if email exists
    fun loopCheckEmail(): Boolean{
        val amountOfEmails = 10 //change based on dbconnection
        var dbEmailInput : String
        var count = 0
        var found = false
        while(count <= amountOfEmails || found == false){
            dbEmailInput = "enter db connection"
            found = checkEmail(dbEmailInput)
            count++
        }

        return found

    }


    //this may be redundent will update when sure
    fun checkEmail(dbEmail: String): Boolean{
        if (emailInput.text.toString().equals(dbEmail)){
            return true
        }else{
            return false
        }

    }




//Function is not called yet. Will do so when everything is neet and done
    fun passChecksToSignIn(email : String, password: String){

        //Checks if email & password is empty if it is then it
        //returns nothing and displays prompt
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        //Signin call for firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmailAndPassword:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in, navigate to main screen
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // prevents going back to login screen
        } else {
            // Sign in failed, optionally clear the input fields
            binding.emailInput.text?.clear()
            binding.passwordInput.text?.clear()
        }
    }
}