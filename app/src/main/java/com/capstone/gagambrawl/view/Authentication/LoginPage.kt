package com.capstone.gagambrawl.view.Authentication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.capstone.gagambrawl.view.Dashboard.DashboardPage
import com.capstone.gagambrawl.R
class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Set up the button with a slow fade transition
        val toRegister = findViewById<TextView>(R.id.lp_et_toRegister)
        toRegister.setOnClickListener {
            // Create an intent to navigate to LoginPage
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)

            // Apply custom fade in/out animation with 2-second duration
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

        val toFPaswword = findViewById<TextView>(R.id.lp_et_forgotPassword)
        toFPaswword.setOnClickListener {
            // Create an intent to navigate to LoginPage
            val intent = Intent(this, ForgotPasswordPage::class.java)
            startActivity(intent)

            // Apply custom fade in/out animation with 2-second duration
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

        val loginBtn = findViewById<Button>(R.id.lp_et_loginBtn)
        loginBtn.setOnClickListener {
            // Create an intent to navigate to LoginPage
            val intent = Intent(this, DashboardPage::class.java)
            startActivity(intent)

            // Apply custom fade in/out animation with 2-second duration
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

    }
}