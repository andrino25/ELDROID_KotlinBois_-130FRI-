package com.capstone.gagambrawl.view.Dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.adapter.CatalogHomeAdapter
import com.capstone.gagambrawl.adapter.HomeInventoryAdapter
import com.capstone.gagambrawl.databinding.FragmentHomeBinding
import com.capstone.gagambrawl.model.Catalog
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.viewmodel.SharedViewModel
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var catalogHomeAdapter: CatalogHomeAdapter
    private lateinit var homeInventoryAdapter: HomeInventoryAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupClickListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        fetchCatalogs()

        val token = arguments?.getString("token") 
            ?: activity?.intent?.getStringExtra("token") 
            ?: return
        
        sharedViewModel.loadSpiders("Bearer $token")
    }

    private fun setupRecyclerView() {
        catalogHomeAdapter = CatalogHomeAdapter()
        binding.homeCatalogRecyclerView.apply {
            adapter = catalogHomeAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        homeInventoryAdapter = HomeInventoryAdapter { /* Do nothing */ }
        binding.inventoryRecyclerView.apply {
            adapter = homeInventoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.showAllBtn.setOnClickListener {
            // Get the activity as DashboardPage
            val dashboardActivity = activity as? DashboardPage
            if (dashboardActivity != null) {
                // Update bottom navigation and load inventory fragment using the activity's method
                dashboardActivity.bottomNavigationView.selectedItemId = R.id.nav_inventory
            }
        }
    }

    private fun fetchCatalogs() {
        // Show loading spinner initially
        binding.homeLoadingSpinner.visibility = View.VISIBLE
        binding.homeCatalogRecyclerView.visibility = View.GONE

        if (!catalogHomeAdapter.isEmpty()) {
            // Data is already loaded, just hide the spinner and show the RecyclerView
            binding.homeLoadingSpinner.visibility = View.GONE
            binding.homeCatalogRecyclerView.visibility = View.VISIBLE
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://gagambrawl-api.vercel.app/api/api/catalogs")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    binding.homeLoadingSpinner.visibility = View.GONE
                    binding.homeCatalogRecyclerView.visibility = View.VISIBLE
                    
                    Toast.makeText(context, "Failed to load catalogs: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "Failed to load catalogs", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val catalogs = parseCatalogs(jsonString)
                    activity?.runOnUiThread {
                        catalogHomeAdapter.updateCatalogs(catalogs)
                        
                        binding.homeLoadingSpinner.visibility = View.GONE
                        binding.homeCatalogRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    // Add this method if you need to force refresh the data
    private fun forceRefreshCatalogs() {
        catalogHomeAdapter.clearCache()
        fetchCatalogs()
    }

    private fun parseCatalogs(jsonString: String): List<Catalog> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val catalogs = mutableListOf<Catalog>()
            
            for (i in 0 until minOf(jsonArray.length(), 4)) {
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d("HomeFragment", "Parsing JSON object: $jsonObject")
                
                catalogs.add(
                    Catalog(
                        catalogId = jsonObject.optInt("_id", 
                                  jsonObject.optInt("id", 
                                  jsonObject.optInt("catalogId", 0))),
                        catalogName = jsonObject.optString("name", 
                                    jsonObject.optString("catalogName", "")),
                        catalogDescription = jsonObject.optString("description",
                                           jsonObject.optString("catalogDescription", "")),
                        catalogImageRef = jsonObject.optString("imageUrl",
                                        jsonObject.optString("image",
                                        jsonObject.optString("catalogImageRef", "")))
                    )
                )
            }
            catalogs
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing catalogs", e)
            Log.e("HomeFragment", "JSON string that failed: $jsonString")
            emptyList()
        }
    }

    private fun setupObservers() {
        sharedViewModel.spiders.observe(viewLifecycleOwner) { spiders ->
            spiders?.let {
                // Only show first 4 spiders in home screen
                val limitedSpiders = if (it.size > 4) it.take(4) else it
                homeInventoryAdapter.updateSpiders(limitedSpiders)
            }
        }
    }

    private fun showSpiderDetails(spider: Spider) {
        val bundle = Bundle().apply {
            putString("spiderId", spider.id)
            putString("token", arguments?.getString("token"))
        }

        val detailsFragment = InventorySpiderDetailsFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}