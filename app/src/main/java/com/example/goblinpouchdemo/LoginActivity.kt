package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity(){
    lateinit var emailInput: EditText
    lateinit var passwordInput: EditText


    //Will check if email is found first then if password is correct from db or smth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //this may be redundent might edit later
        assignVars()

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

    fun checkEmail(dbEmail: String): Boolean{
        if (emailInput.text.toString().equals(dbEmail)){
            return true
        }else{
            return false
        }

    }


    //seperating actions into funcs so it is easier to follow main code
    fun assignVars(){
        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)

    }
}