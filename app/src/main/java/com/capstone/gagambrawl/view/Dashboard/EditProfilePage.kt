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

class EditProfilePage : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var firstNameET: EditText
    private lateinit var middleNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var addressET: EditText
    private lateinit var headerContainer: RelativeLayout
    private var token: String = ""
    private var hasChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_page)

        firstNameET = findViewById(R.id.firstNameET)
        middleNameET = findViewById(R.id.middleNameET)
        lastNameET = findViewById(R.id.lastNameET)
        addressET = findViewById(R.id.addressET)
        headerContainer = findViewById(R.id.headerContainer)

        // Get all user data from intent
        token = intent.getStringExtra("token") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        
        Log.d("EditProfilePage", "Received address: $address")
        
        firstNameET.setText(intent.getStringExtra("firstName"))
        middleNameET.setText(intent.getStringExtra("middleName"))
        lastNameET.setText(intent.getStringExtra("lastName"))
        addressET.setText(address)

        setupSaveButton()
        setupTextChangeListeners()

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
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

                val updateData = mapOf(
                    "_method" to "PATCH",
                    "userFirstName" to firstNameET.text.toString().takeIf { it.isNotEmpty() },
                    "userMiddleName" to middleNameET.text.toString().takeIf { it.isNotEmpty() },
                    "userLastName" to lastNameET.text.toString().takeIf { it.isNotEmpty() },
                    "userAddress" to addressET.text.toString().takeIf { it.isNotEmpty() }
                )

                Log.d("EditProfilePage", "Making API request with data: $updateData")

                val response = apiService.updateUserProfile("Bearer $token", updateData)
                
                Toast.makeText(this@EditProfilePage, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                saveButton.visibility = View.GONE
                hasChanges = false
                
                // Set result OK so ProfileFragment knows to refresh
                setResult(RESULT_OK)
                finish() // Close the edit page after successful update
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
}