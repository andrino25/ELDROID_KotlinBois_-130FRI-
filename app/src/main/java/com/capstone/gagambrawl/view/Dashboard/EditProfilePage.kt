package com.capstone.gagambrawl.view.Dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import com.capstone.gagambrawl.R
import android.view.View
import android.widget.RelativeLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import com.capstone.gagambrawl.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.ImageView
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import android.provider.OpenableColumns
import java.io.File
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class EditProfilePage : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var firstNameET: EditText
    private lateinit var middleNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var addressET: EditText
    private lateinit var headerContainer: RelativeLayout
    private var token: String = ""
    private var hasChanges = false
    private lateinit var userProfilePicRef: ImageView
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            userProfilePicRef.setImageURI(it)
            hasChanges = true
            saveButton.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_page)

        firstNameET = findViewById(R.id.firstNameET)
        middleNameET = findViewById(R.id.middleNameET)
        lastNameET = findViewById(R.id.lastNameET)
        addressET = findViewById(R.id.addressET)
        headerContainer = findViewById(R.id.headerContainer)
        userProfilePicRef = findViewById(R.id.userProfilePicRef)

        // Get all user data from intent
        token = intent.getStringExtra("token") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        
        Log.d("EditProfilePage", "Received address: $address")
        
        firstNameET.setText(intent.getStringExtra("firstName"))
        middleNameET.setText(intent.getStringExtra("middleName"))
        lastNameET.setText(intent.getStringExtra("lastName"))
        addressET.setText(address)


        // Load existing profile picture if available
        val profilePicUrl = intent.getStringExtra("userProfilePicRef")
        if (!profilePicUrl.isNullOrEmpty()) {
            Log.d("EditProfilePage", "Loading profile pic from URL: $profilePicUrl")
            Glide.with(this)
                .load(profilePicUrl)
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(userProfilePicRef)
        } else {
            Log.d("EditProfilePage", "No profile pic URL available")
            userProfilePicRef.setImageResource(R.drawable.ic_default_profile)
        }

        setupSaveButton()
        setupTextChangeListeners()

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // Setup edit profile picture button
        findViewById<ImageButton>(R.id.editProfilePicButton).setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun setupSaveButton() {
        saveButton = Button(this).apply {
            text = "Save"
            setTextColor(getColor(R.color.white))
            background = null
            visibility = View.GONE
        }

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_END)
            addRule(RelativeLayout.CENTER_VERTICAL)
        }

        headerContainer.addView(saveButton, params)

        saveButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun setupTextChangeListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasChanges = true
                saveButton.visibility = View.VISIBLE
            }
        }

        firstNameET.addTextChangedListener(textWatcher)
        middleNameET.addTextChangedListener(textWatcher)
        lastNameET.addTextChangedListener(textWatcher)
        addressET.addTextChangedListener(textWatcher)
    }

    private fun updateProfile() {
        if (!hasChanges) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://gagambrawl-api.vercel.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiService = retrofit.create(ApiService::class.java)

                // Create RequestBody instances for text fields
                val methodPart = RequestBody.create("text/plain".toMediaTypeOrNull(), "PATCH")
                val firstNamePart = firstNameET.text.toString().takeIf { it.isNotEmpty() }?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val middleNamePart = middleNameET.text.toString().takeIf { it.isNotEmpty() }?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val lastNamePart = lastNameET.text.toString().takeIf { it.isNotEmpty() }?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val addressPart = addressET.text.toString().takeIf { it.isNotEmpty() }?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }

                // Create MultipartBody.Part for image if selected
                val imagePart = selectedImageUri?.let { uri ->
                    // Get the file name from the URI
                    val fileName = getFileName(uri) ?: "profile_image.jpg"
                    
                    // Convert URI to actual file
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, fileName)
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Create RequestBody from file
                    val requestFile = RequestBody.create(
                        (contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull(),
                        file
                    )

                    // Create MultipartBody.Part
                    MultipartBody.Part.createFormData("userProfilePicRef", fileName, requestFile)
                }

                Log.d("EditProfilePage", "Uploading image: ${imagePart != null}")

                val response = apiService.updateUserProfileWithImage(
                    "Bearer $token",
                    methodPart,
                    firstNamePart,
                    middleNamePart,
                    lastNamePart,
                    addressPart,
                    imagePart
                )

                Log.d("EditProfilePage", "Profile update response: $response")
                
                Toast.makeText(this@EditProfilePage, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                saveButton.visibility = View.GONE
                hasChanges = false
                
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                when (e) {
                    is retrofit2.HttpException -> {
                        val code = e.code()
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e("EditProfilePage", "HTTP $code Error: $errorBody")
                        Toast.makeText(
                            this@EditProfilePage,
                            "Failed to update profile: HTTP $code",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Log.e("EditProfilePage", "Error: ${e.message}", e)
                        Toast.makeText(
                            this@EditProfilePage,
                            "Failed to update profile: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Helper function to get file name from URI
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }
}