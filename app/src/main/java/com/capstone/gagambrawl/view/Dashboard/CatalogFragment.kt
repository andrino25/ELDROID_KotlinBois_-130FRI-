package com.capstone.gagambrawl.view.Dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.databinding.FragmentCatalogBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.gagambrawl.adapter.CatalogAdapter
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CatalogFragment : Fragment() {
    private lateinit var binding: FragmentCatalogBinding
    private lateinit var catalogAdapter: CatalogAdapter
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gagambrawl-api.vercel.app/api/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        fetchCatalogs()
    }

    private fun setupRecyclerView() {
        catalogAdapter = CatalogAdapter()
        binding.catalogRecyclerView.apply {
            adapter = catalogAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchCatalogs() {
        // Show loading spinner
        binding.loadingSpinner.visibility = View.VISIBLE
        binding.catalogRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = apiService.getCatalogs()
                catalogAdapter.updateCatalogs(response)
                // Hide loading spinner and show RecyclerView
                binding.loadingSpinner.visibility = View.GONE
                binding.catalogRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                // Handle error
                binding.loadingSpinner.visibility = View.GONE
                binding.catalogRecyclerView.visibility = View.VISIBLE
                Toast.makeText(context, "Error loading catalogs: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CatalogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CatalogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}