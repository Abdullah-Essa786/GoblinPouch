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
import com.example.goblinpouchdemo.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity(){

    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    //Will check if email is found first then if password is correct from db or smth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth;
        val currentUser = auth.currentUser

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
//checks if email exists and if the edt boxes are filled when btn is clicked
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
             else {
                // Email found — add navigation here
                passChecksToSignIn(email,password)

            }
        }

    }
    //Creates pop up if password is wrong

    fun popUpMenu(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please Register")
        builder.setMessage("Invalid email or password\nPlease check your credentials or register to continue")
        builder.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton("Register") { dialog, _ ->
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        builder.create().show()

    }


//Function is not called yet. Will do so when everything is neet and done
    fun passChecksToSignIn(email : String, password: String){
        //Signin call for firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmailAndPassword:success")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                    popUpMenu()
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
            binding.etEmail.text?.clear()
            binding.etPassword.text?.clear()
        }
    }
}