package com.example.goblinpouchdemo

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goblinpouchdemo.databinding.TopNavBinding

abstract class NavSetup : AppCompatActivity() {

    protected lateinit var navBinding: TopNavBinding

    protected fun initRootBinding(){
        navBinding = TopNavBinding.inflate(layoutInflater)
        setContentView(navBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(navBinding.header.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(navBinding.bottomNav.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.top, v.paddingRight, systemBars.bottom)
            insets
        }

        navBinding.header.btnMenu.setOnClickListener {
            navBinding.drawerLayout.open()
        }

    }

    protected fun setupCommonNav(){
        navBinding.bottomNav.root.findViewById<View>(R.id.navHome).setOnClickListener {
            if (this !is Dashboard) {
                startActivity(Intent(this, Dashboard::class.java))
            }
        }

        navBinding.bottomNav.root.findViewById<View>(R.id.navProfile).setOnClickListener {
            if (this !is ProfileActivity) {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

    }

}