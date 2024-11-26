package com.capstone.gagambrawl.view.Dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardPage : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var token: String
    private var userFirstName: String? = null
    private var userMiddleName: String? = null
    private var userLastName: String? = null
    private var userEmail: String? = null
    private var userAddress: String? = null
    private var currentFragmentId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_page)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Get token from intent
        token = intent.getStringExtra("token") ?: ""

        fetchUserData()

        // Pass token to initial fragment
        loadFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener { item ->
            // Prevent reloading the same fragment
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
                        }
                    })
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        } catch (e: Exception) {
            // Handle any potential exceptions
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
            } catch (e: Exception) {
            }
        }
    }

    // Override back press to update currentFragmentId
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
}
