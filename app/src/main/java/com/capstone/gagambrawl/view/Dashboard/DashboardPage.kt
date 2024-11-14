package com.capstone.gagambrawl.view.Dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.capstone.gagambrawl.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardPage : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_page)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        //Run Home by default
        loadFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_catalog -> {
                    loadFragment(CatalogFragment())
                    true
                }
                R.id.nav_inventory -> {
                    loadFragment(InventoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

}
