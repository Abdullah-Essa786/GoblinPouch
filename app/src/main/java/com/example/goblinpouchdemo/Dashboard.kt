package com.example.goblinpouchdemo

import com.example.goblinpouchdemo.NavSetup
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.goblinpouchdemo.databinding.ActivityDashboardBinding
import com.example.goblinpouchdemo.databinding.DashboardContentBinding
import com.example.goblinpouchdemo.databinding.TopNavBinding

class Dashboard : NavSetup() {

    private lateinit var contentBinding: DashboardContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initRootBinding()
        setupCommonNav()

        navBinding.header.tvPageTitle.text = "Dashboard"

        val frame = navBinding.pageContent
        val contentView = layoutInflater.inflate(R.layout.dashboard_content, frame, false)
        frame.addView(contentView)
        contentBinding = DashboardContentBinding.bind(contentView)

    }

}