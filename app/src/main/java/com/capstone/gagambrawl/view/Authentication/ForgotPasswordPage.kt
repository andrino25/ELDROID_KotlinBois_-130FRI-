package com.capstone.gagambrawl.view.Authentication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.model.ForgotPasswordCredentials
import com.capstone.gagambrawl.viewmodel.ForgotPasswordViewModel

class ForgotPasswordPage : AppCompatActivity() {
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_passwordpage)

        // Keep existing window setup code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        viewModel = ForgotPasswordViewModel()

        // Keep existing close button logic
        val toLogin = findViewById<ImageView>(R.id.fp_close_btn)
        toLogin.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
        }

        // Add forgot password functionality
        val emailField = findViewById<EditText>(R.id.fp_et_email)
        val passwordField = findViewById<EditText>(R.id.fp_et_password)
        val confirmPasswordField = findViewById<EditText>(R.id.fp_et_confirm_password)
        val submitButton = findViewById<Button>(R.id.fp_submitButton)

        submitButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val newPassword = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailField.error = "Email is required"
                    return@setOnClickListener
                }
                newPassword.isEmpty() -> {
                    passwordField.error = "New password is required"
                    return@setOnClickListener
                }
                confirmPassword.isEmpty() -> {
                    confirmPasswordField.error = "Confirm password is required"
                    return@setOnClickListener
                }
                newPassword != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Show loading dialog
            viewModel.showLoadingDialog(this)
            submitButton.isEnabled = false

            val credentials = ForgotPasswordCredentials(
                email = email,
                new_password = newPassword,
                new_password_confirmation = confirmPassword
            )

            viewModel.resetPassword(credentials) { result ->
                runOnUiThread {
                    // Dismiss loading dialog
                    viewModel.dismissLoadingDialog()
                    submitButton.isEnabled = true

                    when (result) {
                        is ForgotPasswordViewModel.ForgotPasswordResult.Success -> {
                            Toast.makeText(this, "Password reset successful! Please login with your new password.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, LoginPage::class.java)
                            startActivity(intent)
                            finish()
                            overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
                        }
                        is ForgotPasswordViewModel.ForgotPasswordResult.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}