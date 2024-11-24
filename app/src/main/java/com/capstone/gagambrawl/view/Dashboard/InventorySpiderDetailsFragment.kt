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
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.utils.Constants

class InventorySpiderDetailsFragment : Fragment() {
    private var spiderId: String? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            spiderId = it.getString(Constants.BundleKeys.SPIDER_ID)
            token = it.getString(Constants.BundleKeys.TOKEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory_spider_details, container, false)

        // Initialize views
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        val spiderImage = view.findViewById<ImageView>(R.id.spiderImage)
        val spiderNameText = view.findViewById<TextView>(R.id.spiderName)
        val spiderSizeText = view.findViewById<TextView>(R.id.spiderSize)
        val spiderStatusText = view.findViewById<TextView>(R.id.spiderStatus)
        val spiderDescriptionText = view.findViewById<TextView>(R.id.spiderDescription)
        val editButton = view.findViewById<Button>(R.id.editDetailsButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteDetailsButton)
        val favoriteIcon = view.findViewById<ImageView>(R.id.favoriteIcon)

        // Set values from arguments
        arguments?.let { args ->
            spiderNameText.text = args.getString("spiderName", "")
            spiderSizeText.text = args.getString("spiderSize", "")
            spiderStatusText.text = args.getString("spiderHealth", "")
            spiderDescriptionText.text = args.getString("spiderDescription", "")

            // Load spider image
            args.getString("spiderImage")?.let { imageUrl ->
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.spider_image_placeholder)
                    .error(R.drawable.spider_image_placeholder)
                    .into(spiderImage)
            }
        }

        // Set click listeners
        backButton.setOnClickListener {
            // Go back to previous fragment
            parentFragmentManager.popBackStack()
        }

        editButton.setOnClickListener {
            // TODO: Implement edit functionality
            Toast.makeText(context, "Edit functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        deleteButton.setOnClickListener {
            // TODO: Implement delete functionality
            Toast.makeText(context, "Delete functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        favoriteIcon.setOnClickListener {
            // TODO: Implement favorite functionality
            Toast.makeText(context, "Favorite functionality coming soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(spiderId: String, token: String) =
            InventorySpiderDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("spiderId", spiderId)
                    putString("token", token)
                }
            }
    }
}