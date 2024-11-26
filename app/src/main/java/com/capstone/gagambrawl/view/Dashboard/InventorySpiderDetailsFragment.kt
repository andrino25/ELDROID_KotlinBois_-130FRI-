package com.capstone.gagambrawl.view.Dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.databinding.FragmentInventorySpiderDetailsBinding
import com.capstone.gagambrawl.model.Spider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class InventorySpiderDetailsFragment : Fragment() {
    private var _binding: FragmentInventorySpiderDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var spider: Spider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventorySpiderDetailsBinding.inflate(inflater, container, false)
        
        // Get spider from arguments
        arguments?.getParcelable<Spider>("spider")?.let {
            spider = it
            displaySpiderDetails()
        }

        setupClickListeners()
        return binding.root
    }

    private fun displaySpiderDetails() {
        binding.apply {
            spiderName.text = spider.spiderName
            spiderSize.text = "Size: ${spider.spiderSize}"
            spiderStatus.text = "Status: ${spider.spiderHealthStatus}"
            spiderDescription.text = spider.spiderDescription

            // Set status color based on health status
            val statusColor = when (spider.spiderHealthStatus) {
                "Healthy" -> requireContext().getColor(R.color.status_healthy)
                "Injured" -> requireContext().getColor(R.color.status_injured)
                "Unavailable" -> requireContext().getColor(R.color.status_unavailable)
                else -> requireContext().getColor(R.color.gray)
            }
            spiderStatus.setTextColor(statusColor)

            // Load spider image
            Glide.with(requireContext())
                .load(spider.spiderImageRef)
                .placeholder(R.drawable.spider_image_placeholder)
                .error(R.drawable.spider_image_placeholder)
                .into(spiderImage)

            // Make all views visible since we have the data
            spiderImage.visibility = View.VISIBLE
            spiderName.visibility = View.VISIBLE
            spiderSize.visibility = View.VISIBLE
            spiderStatus.visibility = View.VISIBLE
            spiderDescriptionLabel.visibility = View.VISIBLE
            spiderDescription.visibility = View.VISIBLE
            favoriteIcon.visibility = View.VISIBLE
            
            // Hide loading spinner
            loadingSpinner.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            editDetailsButton.setOnClickListener {
                Toast.makeText(context, "Edit functionality coming soon", Toast.LENGTH_SHORT).show()
            }

            deleteDetailsButton.setOnClickListener {
                Toast.makeText(context, "Delete functionality coming soon", Toast.LENGTH_SHORT).show()
            }

            favoriteIcon.setOnClickListener {
                Toast.makeText(context, "Favorite functionality coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(spider: Spider) = InventorySpiderDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("spider", spider)
            }
        }
    }
}