package com.example.goblinpouchdemo

import android.content.ContentValues.TAG
import android.content.Intent
import android.widget.Button
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.goblinpouchdemo.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.goblinpouchdemo.models.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database


class RegisterActivity : AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding= RegisterActivity.inflate(layoutInflater)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        val btnRegister = binding.btnRegister

        //Later: save user to Fire Base
        btnRegister.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && binding.etName.text.toString().isNotEmpty() && binding.etAge.text.toString().isNotEmpty() && binding.etPhone.text.toString().isNotEmpty()){
                registerUser(email, password)
            }else{
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun registerUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid ?: ""

                    val user = User(
                        uid = userId,
                        username = binding.etName.text.toString(),
                        email = email,
                        age = binding.etAge.text.toString().toIntOrNull() ?: 0,
                        phone = binding.etPhone.text.toString(),
                        joinDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    )

                    dbRef = Firebase.database.getReference("Users")
                    dbRef.child(userId).child("profile").setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val intent = Intent(this, Dashboard::class.java)
                            intent.putExtra("USER_ID", userId)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to register user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }

    }
}