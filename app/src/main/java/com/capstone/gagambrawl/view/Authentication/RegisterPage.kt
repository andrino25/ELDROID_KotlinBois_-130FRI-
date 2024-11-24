package com.capstone.gagambrawl.view.Authentication

import RegisterViewModel
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.capstone.gagambrawl.R
import android.widget.Toast
import android.widget.EditText
import android.widget.Button
import com.capstone.gagambrawl.model.RegisterCredentiials
import android.text.Editable
import android.text.TextWatcher


class RegisterPage : AppCompatActivity() {
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)


        val toLogin = findViewById<TextView>(R.id.rp_et_toLogin)
        toLogin.setOnClickListener {
            // Create an intent to navigate to LoginPage
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)

            // Apply custom fade in/out animation with 2-second duration
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

        viewModel = RegisterViewModel()
        viewModel.setupWindowFullscreen(this)

        // Get references to the EditText fields
        val emailField = findViewById<EditText>(R.id.rp_et_email)
        val passwordField = findViewById<EditText>(R.id.rp_et_password)
        val confirmPasswordField = findViewById<EditText>(R.id.rp_et_confirm_password)
        val registerButton = findViewById<Button>(R.id.rp_et_regBtn)

        // Handle register button click
        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Confirm password is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                confirmPassword.length < 6 -> {
                    Toast.makeText(this, "Confirm password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            // Show loading dialog
            viewModel.showLoadingDialog(this)
            registerButton.isEnabled = false

            val credentials = RegisterCredentiials(
                email = email,
                password = password,
                password_confirmation = confirmPassword
            )
            
            viewModel.registerUser(credentials) { result ->
                runOnUiThread {
                    // Dismiss loading dialog
                    viewModel.dismissLoadingDialog()
                    registerButton.isEnabled = true
                    
                    when (result) {
                        is RegisterViewModel.RegisterResult.Success -> {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginPage::class.java)
                            startActivity(intent)
                            finish()
                            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
                        }
                        is RegisterViewModel.RegisterResult.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}