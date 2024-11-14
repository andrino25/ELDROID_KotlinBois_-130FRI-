package com.capstone.gagambrawl.view.Authentication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.capstone.gagambrawl.R
class ForgotPasswordPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_passwordpage)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val toLogin = findViewById<ImageView>(R.id.fp_close_btn)
        toLogin.setOnClickListener {
            // Create an intent to navigate to LoginPage
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)

            // Apply custom fade in/out animation with 2-second duration
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

    }
}