package com.capstone.gagambrawl.view.Dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.gagambrawl.adapter.CatalogHomeAdapter
import com.capstone.gagambrawl.databinding.FragmentHomeBinding
import com.capstone.gagambrawl.model.Catalog
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var catalogHomeAdapter: CatalogHomeAdapter

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchCatalogs()
    }

    private fun setupRecyclerView() {
        catalogHomeAdapter = CatalogHomeAdapter()
        binding.homeCatalogRecyclerView.apply {
            adapter = catalogHomeAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    private fun fetchCatalogs() {
        // Show loading spinner, hide recycler view
        binding.homeLoadingSpinner.visibility = View.VISIBLE
        binding.homeCatalogRecyclerView.visibility = View.GONE

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://gagambrawl-api.vercel.app/api/api/catalogs")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    // Hide loading spinner, show recycler view
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
                        
                        // Hide loading spinner, show recycler view
                        binding.homeLoadingSpinner.visibility = View.GONE
                        binding.homeCatalogRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}