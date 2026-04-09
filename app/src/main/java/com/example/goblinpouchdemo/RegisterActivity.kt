package com.example.goblinpouchdemo

import android.content.Intent
import android.widget.Button
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class RegisterActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)


        //Later: save user to Fire Base
        btnRegister.setOnClickListener {


            val intent = Intent(this, LoginActivity::class.java)
            //runs the login page
            startActivity(intent)
            // closes the register page
            finish()




}

}
}