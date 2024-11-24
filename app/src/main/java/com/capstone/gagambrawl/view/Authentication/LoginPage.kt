package com.capstone.gagambrawl.view.Authentication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.Toast
import com.capstone.gagambrawl.view.Dashboard.DashboardPage
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.model.LoginCredentials
import com.capstone.gagambrawl.viewmodel.LoginViewModel

class LoginPage : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        viewModel = LoginViewModel()
        viewModel.setupWindowFullscreen(this)

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

        val emailField = findViewById<EditText>(R.id.lp_et_email)
        val passwordField = findViewById<EditText>(R.id.lp_et_password)
        val loginBtn = findViewById<Button>(R.id.lp_et_loginBtn)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailField.error = "Email is required"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    passwordField.error = "Password is required"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Show loading dialog
            viewModel.showLoadingDialog(this)
            loginBtn.isEnabled = false

            val credentials = LoginCredentials(
                email = email,
                password = password
            )

            viewModel.loginUser(credentials) { result ->
                runOnUiThread {
                    // Dismiss loading dialog
                    viewModel.dismissLoadingDialog()
                    loginBtn.isEnabled = true

                    when (result) {
                        is LoginViewModel.LoginResult.Success -> {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, DashboardPage::class.java).apply {
                                putExtra("token", result.token)
                                putExtra("email", result.email)
                            }
                            startActivity(intent)
                            finish()
                            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
                        }
                        is LoginViewModel.LoginResult.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }
}