package com.capstone.gagambrawl.viewmodel

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.model.LoginCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowInsetsController
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.utils.SessionManager

class LoginViewModel : ViewModel() {
    private var loadingDialog: AlertDialog? = null

    sealed class LoginResult {
        data class Success(val token: String, val email: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    fun showLoadingDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.pre_loader, null)
        
        dialogBuilder.setView(dialogView)
        loadingDialog = dialogBuilder.create().apply {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun dismissLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        loadingDialog = null
    }

    fun loginUser(credentials: LoginCredentials, context: Context, callback: (LoginResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("email", credentials.email)
                    put("password", credentials.password)
                }

                Log.d("LoginViewModel", "Request Body: ${json}")

                val request = Request.Builder()
                    .url("https://gagambrawl-api.vercel.app/api/api/login")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d("LoginViewModel", "Response Code: ${response.code}")
                    Log.d("LoginViewModel", "Response Body: $responseBody")

                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccessful && responseBody != null -> {
                                val jsonResponse = JSONObject(responseBody)
                                val token = jsonResponse.getString("token")
                                
                                // Save session data
                                val sessionManager = SessionManager(context)
                                sessionManager.saveAuthToken(token)
                                sessionManager.saveUserEmail(credentials.email)
                                
                                callback(LoginResult.Success(token, credentials.email))
                            }
                            response.code == 401 -> {
                                callback(LoginResult.Error("Invalid credentials"))
                            }
                            response.code == 404 -> {
                                callback(LoginResult.Error("User not found"))
                            }
                            else -> {
                                callback(LoginResult.Error("Login failed. Please try again."))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login Error", e)
                withContext(Dispatchers.Main) {
                    callback(LoginResult.Error("Network error: ${e.message}"))
                }
            }
        }
    }

    fun setupWindowFullscreen(activity: Activity) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}