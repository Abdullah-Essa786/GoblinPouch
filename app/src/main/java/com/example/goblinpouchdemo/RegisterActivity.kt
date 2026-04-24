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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class RegisterActivity : AppCompatActivity(){

    lateinit var regEmail: EditText
    lateinit var regPassword: EditText
//    lateinit var regName: EditText
//    lateinit var regAge: EditText
//    lateinit var regConfirmPassword: EditText
//    lateinit var regGender: Spinner
//


    private lateinit var auth: FirebaseAuth
    private lateinit var binding: RegisterActivity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding= RegisterActivity.inflate(layoutInflater)

        setContentView(R.layout.activity_register)

        auth= FirebaseAuth.getInstance()

        val btnRegister = findViewById<Button>(R.id.btnRegister)


        //Later: save user to Fire Base
        btnRegister.setOnClickListener {

            val email = regEmail.text.toString()
            val password = regPassword.text.toString()


            if(email.isNotEmpty()&&password.isNotEmpty()){
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
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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