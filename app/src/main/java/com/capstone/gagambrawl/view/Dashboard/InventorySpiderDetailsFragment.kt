package com.capstone.gagambrawl.view.Dashboard

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.databinding.FragmentInventorySpiderDetailsBinding
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.utils.SessionManager
import com.capstone.gagambrawl.viewmodel.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import android.app.Activity
import android.content.Intent

class InventorySpiderDetailsFragment : Fragment() {
    private var _binding: FragmentInventorySpiderDetailsBinding? = null
    private val binding get() = _binding!!
    private var token: String = ""
    private lateinit var spider: Spider
    private val viewModel: InventoryViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null
    private var editSpiderDialog: Dialog? = null
    private val PICK_IMAGE_REQUEST = 1
    private var isUserAction = false

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

        token = arguments?.getString("token") ?:
                activity?.intent?.getStringExtra("token") ?: ""
        if (!token.startsWith("Bearer ")) {
            token = "Bearer $token"
        }

        setupClickListeners()
        return binding.root
    }

    private fun displaySpiderDetails() {
        binding.apply {
            spiderName.text = spider.spiderName
            spiderSize.text = "Size: ${spider.spiderSize}"
            spiderValue.text = "Market Value: ₱${spider.spiderEstimatedMarketValue}"
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
            spiderValue.visibility = View.VISIBLE
            spiderStatus.visibility = View.VISIBLE
            spiderDescriptionLabel.visibility = View.VISIBLE
            spiderDescription.visibility = View.VISIBLE
            favoriteIcon.visibility = View.VISIBLE
            
            // Hide loading spinner
            loadingSpinner.visibility = View.GONE

            // Update favorite icon based on spider's favorite status
            favoriteIcon.setImageResource(
                if (spider.spiderIsFavorite == 1) R.drawable.ic_star_on
                else R.drawable.ic_star_off
            )
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            editDetailsButton.setOnClickListener {
                showEditSpiderDialog()
            }

            deleteDetailsButton.setOnClickListener {
                showDeleteConfirmationDialog()
            }

            favoriteIcon.setOnClickListener {
                isUserAction = true
                viewModel.toggleFavorite(
                    token = token,
                    spiderId = spider.spiderId,
                    isFavorite = spider.spiderIsFavorite,
                    spider = spider,
                    context = requireContext()
                )
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_delete)
        
        // Setup dialog buttons
        dialog.findViewById<ImageButton>(R.id.i_close_btn).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.findViewById<Button>(R.id.btn_DeleteSpider).setOnClickListener {
            var token = requireActivity().intent.getStringExtra("token") ?:
                       SessionManager(requireContext()).fetchAuthToken() ?: ""
            
            if (!token.startsWith("Bearer ")) {
                token = "Bearer $token"
            }
            
            viewModel.deleteSpider(token, spider.spiderId)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showEditSpiderDialog() {
        editSpiderDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_edit_spider)

            // Set existing values
            findViewById<EditText>(R.id.edit_dialog_spider_name).setText(spider.spiderName)
            findViewById<EditText>(R.id.edit_dialog_spider_value).setText(spider.spiderEstimatedMarketValue.toString())
            findViewById<EditText>(R.id.edit_dialog_spider_description).setText(spider.spiderDescription)

            // Setup spinners
            setupSpinners(this, spider.spiderHealthStatus, spider.spiderSize)

            // Load existing image
            val imageView = findViewById<ImageView>(R.id.edit_image_view)
            Glide.with(requireContext())
                .load(spider.spiderImageRef)
                .into(imageView)

            // Setup click listeners
            findViewById<ImageButton>(R.id.i_close_btn).setOnClickListener {
                dismiss()
            }

            findViewById<RelativeLayout>(R.id.dialog_spider_desc).setOnClickListener {
                pickImage()
            }

            findViewById<Button>(R.id.dialog_edit_spiderBtn).setOnClickListener {
                val name = findViewById<EditText>(R.id.edit_dialog_spider_name).text.toString()
                val health = findViewById<Spinner>(R.id.edit_dialog_spider_health).selectedItem.toString()
                val size = findViewById<Spinner>(R.id.edit_dialog_spider_size).selectedItem.toString()
                val value = findViewById<EditText>(R.id.edit_dialog_spider_value).text.toString().toDoubleOrNull()
                val description = findViewById<EditText>(R.id.edit_dialog_spider_description).text.toString()

                var token = requireActivity().intent.getStringExtra("token") ?:
                           SessionManager(requireContext()).fetchAuthToken() ?: ""
                
                if (!token.startsWith("Bearer ")) {
                    token = "Bearer $token"
                }

                viewModel.updateSpider(
                    token,
                    spider.spiderId.toString(),
                    name,
                    health,
                    size,
                    value,
                    description,
                    selectedImageUri,
                    requireContext(),
                    spider,
                    dialog = editSpiderDialog
                )
            }

            show()
        }
    }

    private fun setupSpinners(dialog: Dialog, currentHealth: String, currentSize: String) {
        val healthSpinner = dialog.findViewById<Spinner>(R.id.edit_dialog_spider_health)
        val sizeSpinner = dialog.findViewById<Spinner>(R.id.edit_dialog_spider_size)

        // Setup health spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.health_status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            healthSpinner.adapter = adapter
            // Set current selection
            val position = adapter.getPosition(currentHealth)
            healthSpinner.setSelection(position)
        }

        // Setup size spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.size_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sizeSpinner.adapter = adapter
            // Set current selection
            val position = adapter.getPosition(currentSize)
            sizeSpinner.setSelection(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let { // Only process if result is not null
                result.fold(
                    onSuccess = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        // Navigate back to inventory
                        parentFragmentManager.popBackStack()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "Failed to delete spider: ${exception.message}", 
                                    Toast.LENGTH_SHORT).show()
                    }
                )
                // Clear the result after handling it
                viewModel.clearDeleteResult()
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.updateSpiderResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (isUserAction) {
                    result.fold(
                        onSuccess = { updatedSpider ->
                            Toast.makeText(context, "Spider updated successfully", Toast.LENGTH_SHORT).show()
                            editSpiderDialog?.dismiss()
                            spider = updatedSpider
                            displaySpiderDetails()
                        },
                        onFailure = { exception ->
                            Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    result.onSuccess { updatedSpider ->
                        spider = updatedSpider
                        displaySpiderDetails()
                    }
                }
                isUserAction = false
                viewModel.clearUpdateSpiderResult()
            }
        }

        viewModel.favoriteToggleResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (isUserAction) {
                    result.fold(
                        onSuccess = { updatedSpider ->
                            spider = updatedSpider
                            binding.favoriteIcon.setImageResource(
                                if (updatedSpider.spiderIsFavorite == 1) R.drawable.ic_star_on
                                else R.drawable.ic_star_off
                            )
                            val message = if (updatedSpider.spiderIsFavorite == 1) 
                                "Added to favorites" 
                            else 
                                "Removed from favorites"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { exception ->
                            Toast.makeText(context, "Failed to update favorite status", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    result.onSuccess { updatedSpider ->
                        spider = updatedSpider
                        binding.favoriteIcon.setImageResource(
                            if (updatedSpider.spiderIsFavorite == 1) R.drawable.ic_star_on
                            else R.drawable.ic_star_off
                        )
                    }
                }
                isUserAction = false
                viewModel.clearFavoriteToggleResult()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editSpiderDialog?.dismiss()
        editSpiderDialog = null
        _binding = null
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            editSpiderDialog?.let { dialog ->
                selectedImageUri?.let { uri ->
                    val imageView = dialog.findViewById<ImageView>(R.id.edit_image_view)
                    Glide.with(requireContext())
                        .load(uri)
                        .into(imageView)
                }
            }
        }
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