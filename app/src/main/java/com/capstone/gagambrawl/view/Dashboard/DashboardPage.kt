package com.capstone.gagambrawl.view.Dashboard

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.utils.SessionManager
import com.capstone.gagambrawl.view.Authentication.LoginPage
import com.capstone.gagambrawl.viewmodel.InventoryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val NOTIFICATION_PERMISSION_CODE = 1001
class DashboardPage : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var token: String
    private var userFirstName: String? = null
    private var userMiddleName: String? = null
    private var userLastName: String? = null
    private var userEmail: String? = null
    private var userAddress: String? = null
    private var userProfilePicRef: String? = null
    private var currentFragmentId = R.id.nav_home
    private lateinit var sessionManager: SessionManager
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_page)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        sessionManager = SessionManager(this)

        // Get token from session manager if not provided in intent
        token = intent.getStringExtra("token") ?: sessionManager.fetchAuthToken() ?: ""

        fetchUserData()

        // Handle notification navigation
        if (intent.getBooleanExtra("open_inventory", false)) {
            val targetSpiderName = intent.getStringExtra("target_spider_name")
            val notificationAction = intent.getStringExtra("notification_action")
            
            // Navigate to inventory with the target spider info
            loadFragment(InventoryFragment().apply {
                arguments = Bundle().apply {
                    putString("token", token)
                    putString("target_spider_name", targetSpiderName)
                    putString("notification_action", notificationAction)
                }
            })
            bottomNavigationView.selectedItemId = R.id.nav_inventory
        } else {
            // Default to home
            loadFragment(HomeFragment())
        }

        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentFragmentId) {
                return@setOnItemSelectedListener true
            }

            currentFragmentId = item.itemId

            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                R.id.nav_catalog -> {
                    loadFragment(CatalogFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                R.id.nav_inventory -> {
                    loadFragment(InventoryFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                        }
                    })
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("token", token)
                            putString("firstName", userFirstName)
                            putString("middleName", userMiddleName)
                            putString("lastName", userLastName)
                            putString("address", userAddress)
                            putString("email", userEmail)
                            putString("userProfilePicRef", userProfilePicRef)
                        }
                    })
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("Please allow notifications to stay updated with important alerts.")
            .setPositiveButton("Grant Permission") { _, _ ->
                checkNotificationPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchUserData() {
        val apiService = Retrofit.Builder()
            .baseUrl("https://gagambrawl-api.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = apiService.getUserProfile("Bearer $token")
                userFirstName = user.userFirstName
                userMiddleName = user.userMiddleName
                userLastName = user.userLastName
                userEmail = user.email
                userAddress = user.userAddress
                userProfilePicRef = user.userProfilePicRef
            } catch (e: Exception) {
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        when (currentFragment) {
            is HomeFragment -> currentFragmentId = R.id.nav_home
            is CatalogFragment -> currentFragmentId = R.id.nav_catalog
            is InventoryFragment -> currentFragmentId = R.id.nav_inventory
            is ProfileFragment -> currentFragmentId = R.id.nav_profile
        }
        super.onBackPressed()
    }

    fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToInventory() {
        bottomNavigationView.selectedItemId = R.id.nav_inventory
        loadFragment(InventoryFragment().apply {
            arguments = Bundle().apply {
                putString("token", token)
            }
        })
    }
}

