package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val txtRegister = findViewById<TextView>(R.id.txtRegister)
         // Displays register link

        txtRegister.setOnClickListener {
            //Runs the register page
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}