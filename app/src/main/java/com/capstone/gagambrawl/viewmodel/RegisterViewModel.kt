import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.model.RegisterCredentiials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.capstone.gagambrawl.R

class RegisterViewModel : ViewModel() {
    private var loadingDialog: AlertDialog? = null

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val message: String) : RegisterResult()
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

    fun registerUser(credentials: RegisterCredentiials, callback: (RegisterResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply {
                    put("email", credentials.email)
                    put("password", credentials.password)
                    put("password_confirmation", credentials.password_confirmation)
                }

                Log.d("RegisterViewModel", "Request Body: ${json.toString()}")

                val request = Request.Builder()
                    .url("https://gagambrawl-api.vercel.app/api/api/register")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d("RegisterViewModel", "Response Code: ${response.code}")
                    Log.d("RegisterViewModel", "Response Body: $responseBody")
                    
                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccessful -> {
                                callback(RegisterResult.Success)
                            }
                            response.code == 422 -> {
                                val jsonError = JSONObject(responseBody ?: "")
                                val errorMessage = jsonError.optString("message", "Email already exists")
                                callback(RegisterResult.Error(errorMessage))
                            }
                            else -> {
                                callback(RegisterResult.Error("Registration failed. Please try again."))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Network Error", e)
                withContext(Dispatchers.Main) {
                    callback(RegisterResult.Error("Network error: ${e.message}"))
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
