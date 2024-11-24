package com.capstone.gagambrawl.view.Dashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.api.RetrofitClient
import com.capstone.gagambrawl.adapter.SpiderAdapter
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.utils.Constants
import com.capstone.gagambrawl.utils.FileUtil
import com.capstone.gagambrawl.viewmodel.SharedViewModel
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class InventoryFragment : Fragment() {
    private lateinit var token: String
    private val apiService = RetrofitClient.apiService
    private var selectedImageUri: Uri? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var spiderAdapter: SpiderAdapter
    private var addSpiderDialog: Dialog? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeToken()
    }

    private fun initializeToken() {
        token = arguments?.getString(Constants.BundleKeys.TOKEN) ?: ""
        if (token.isBlank()) {
            token = getString(R.string.param_bearer, 
                activity?.intent?.getStringExtra(Constants.BundleKeys.TOKEN))
        } else if (!token.startsWith(Constants.BEARER_PREFIX)) {
            token = Constants.BEARER_PREFIX + token
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)
        
        recyclerView = view.findViewById(R.id.spidersRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        
        spiderAdapter = SpiderAdapter(emptyList()) { spider ->
            showSpiderDetails(spider)
        }
        recyclerView.adapter = spiderAdapter

        // Observe spiders from SharedViewModel
        sharedViewModel.spiders.observe(viewLifecycleOwner) { spiders ->
            spiders?.let {
                spiderAdapter.updateSpiders(it)
            }
        }

        // Observe loading state
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
        }

        // Observe errors
        sharedViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Load spiders if we don't have them
        if (sharedViewModel.spiders.value == null) {
            sharedViewModel.loadSpiders(token)
        }

        val addBtn: ShapeableImageView = view.findViewById(R.id.addBtn)
        addBtn.setOnClickListener {
            showAddSpiderDialog()
        }

        return view
    }

    private fun showAddSpiderDialog() {
        addSpiderDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_add_spider)

            val imageContainer = findViewById<RelativeLayout>(R.id.dialog_spider_desc)
            val addButton = findViewById<Button>(R.id.dialog_add_spiderBtn)
            
            imageContainer.setOnClickListener {
                pickImage()
            }

            addButton.setOnClickListener {
                // Disable the button immediately
                addButton.isEnabled = false
                
                val name = findViewById<EditText>(R.id.dialog_spider_name).text.toString()
                val health = findViewById<EditText>(R.id.dialog_spider_health).text.toString()
                val size = findViewById<EditText>(R.id.dialog_spider_size).text.toString()
                val value = findViewById<EditText>(R.id.dialog_spider_value).text.toString().toDoubleOrNull() ?: 0.0
                val description = findViewById<EditText>(R.id.dialog_spider_description).text.toString()

                if (validateInputs(name, health, size, value, description)) {
                    addSpider(name, health, size, value, description, this)
                } else {
                    // Re-enable the button if validation fails
                    addButton.isEnabled = true
                }
            }

            show()
        }
    }

    private fun addSpider(
        name: String,
        health: String,
        size: String,
        value: Double,
        description: String,
        dialog: Dialog
    ) {
        lifecycleScope.launch {
            try {
                val imageFile = selectedImageUri?.let { uri ->
                    context?.let { ctx ->
                        FileUtil.getFileFromUri(ctx, uri)
                    }
                } ?: throw Exception("Image is required")

                val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                val imagePart = MultipartBody.Part.createFormData(
                    "spiderImageRef",
                    imageFile.name,
                    imageRequestBody
                )

                val spider = apiService.addSpider(
                    token,
                    name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    health.toRequestBody("text/plain".toMediaTypeOrNull()),
                    size.toRequestBody("text/plain".toMediaTypeOrNull()),
                    value.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    imagePart
                )

                // Refresh the spiders list after successful add
                sharedViewModel.refreshSpiders(token)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Spider added successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to add spider: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Re-enable the button on error
                    dialog.findViewById<Button>(R.id.dialog_add_spiderBtn)?.isEnabled = true
                }
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = getString(R.string.content_type_image)
        }
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.dialog_select_picture)),
            Constants.PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            // Use the stored dialog reference
            addSpiderDialog?.let { updateImagePreview(it) }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InventoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InventoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun validateInputs(
        name: String,
        health: String,
        size: String,
        value: Double,
        description: String
    ): Boolean {
        when {
            name.isBlank() || health.isBlank() || size.isBlank() || description.isBlank() -> {
                showToast(R.string.error_all_fields_required)
                return false
            }
            value <= 0 -> {
                showToast(R.string.error_invalid_market_value)
                return false
            }
            selectedImageUri == null -> {
                showToast(R.string.error_image_required)
                return false
            }
        }
        return true
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun String.toRequestBody(mediaType: MediaType?) =
        RequestBody.create(mediaType, this)

    private fun showSpiderDetails(spider: Spider) {
        // Create bundle with spider details
        val bundle = Bundle().apply {
            putString("spiderId", spider.id)
            putString("spiderName", spider.spiderName)
            putString("spiderHealth", spider.spiderHealthStatus)
            putString("spiderSize", spider.spiderSize)
            putString("spiderValue", spider.spiderEstimatedMarketValue.toString())
            putString("spiderDescription", spider.spiderDescription)
            putString("spiderImage", spider.spiderImageRef)
            putString("token", token) // Pass the token for API calls
        }

        // Create and navigate to details fragment
        val detailsFragment = InventorySpiderDetailsFragment().apply {
            arguments = bundle
        }

        // Replace current fragment with details fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailsFragment)
            .addToBackStack(null)  // Add to back stack so user can return
            .commit()
    }

    private fun updateImagePreview(dialog: Dialog) {
        selectedImageUri?.let { uri ->
            val imageContainer = dialog.findViewById<RelativeLayout>(R.id.dialog_spider_desc)
            val imageView = dialog.findViewById<ImageView>(R.id.add_image_view)
            
            // Hide the default add image icon
            imageView.visibility = View.GONE
            
            // Add a new ImageView for the preview
            val previewImage = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            
            imageContainer.addView(previewImage)
            
            // Load the selected image
            Glide.with(requireContext())
                .load(uri)
                .into(previewImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Dismiss the dialog when the fragment is destroyed
        addSpiderDialog?.dismiss()
        addSpiderDialog = null
    }
}