package com.example.goblinpouchdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // loads up the loading screen (XML)
        setContentView(R.layout.activity_loading)


        Handler(Looper.getMainLooper()).postDelayed({
            //initiate the delay
            val intent = Intent(this, LoginActivity::class.java)
            //runs the next page after the intent is initiated
            startActivity(intent)
            finish()

            // 3 second delay
        }, 3000)

    }
}