package com.capstone.gagambrawl.viewmodel

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.model.ForgotPasswordCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.view.LayoutInflater
import com.capstone.gagambrawl.R
import java.util.concurrent.TimeUnit

class ForgotPasswordViewModel : ViewModel() {
    private var loadingDialog: AlertDialog? = null

    sealed class ForgotPasswordResult {
        object Success : ForgotPasswordResult()
        data class Error(val message: String) : ForgotPasswordResult()
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

    fun resetPassword(credentials: ForgotPasswordCredentials, callback: (ForgotPasswordResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("email", credentials.email)
                    put("newPassword", credentials.new_password)  // Changed to match Laravel
                    put("newPassword_confirmation", credentials.new_password_confirmation)  // Changed to match Laravel
                }

                Log.d("ForgotPasswordViewModel", "Request Body: ${json}")

                val request = Request.Builder()
                    .url("https://gagambrawl-api.vercel.app/api/api/user/forgot-password")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d("ForgotPasswordViewModel", "Response Code: ${response.code}")
                    Log.d("ForgotPasswordViewModel", "Response Body: $responseBody")

                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccessful -> {
                                callback(ForgotPasswordResult.Success)
                            }
                            response.code == 404 -> {
                                callback(ForgotPasswordResult.Error("Email not found"))
                            }
                            response.code == 422 -> {
                                try {
                                    val jsonError = JSONObject(responseBody ?: "")
                                    val errorMessage = jsonError.optString("message", "Invalid input")
                                    callback(ForgotPasswordResult.Error(errorMessage))
                                } catch (e: Exception) {
                                    callback(ForgotPasswordResult.Error("Invalid input format"))
                                }
                            }
                            else -> {
                                callback(ForgotPasswordResult.Error("Password reset failed. Please try again."))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ForgotPasswordViewModel", "Reset Password Error", e)
                withContext(Dispatchers.Main) {
                    val errorMessage = when {
                        e.message?.contains("UnknownHostException") == true -> "No internet connection"
                        e.message?.contains("SocketTimeoutException") == true -> "Connection timed out"
                        else -> "Network error: Please check your connection"
                    }
                    callback(ForgotPasswordResult.Error(errorMessage))
                }
            }
        }
    }
}