package com.capstone.gagambrawl.view.Dashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.adapter.SpiderAdapter
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.viewmodel.InventoryViewModel
import com.google.android.material.imageview.ShapeableImageView
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.widget.ArrayAdapter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class InventoryFragment : Fragment() {
    private var token: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var spiderAdapter: SpiderAdapter
    private var addSpiderDialog: Dialog? = null
    private val viewModel: InventoryViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)
        
        // Initialize token from arguments or intent
        token = arguments?.getString("token") ?: 
                activity?.intent?.getStringExtra("token") ?: ""
        if (!token.startsWith("Bearer ")) {
            token = "Bearer $token"
        }

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.spidersRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        spiderAdapter = SpiderAdapter(emptyList()) { spider ->
            showSpiderDetails(spider)
        }
        recyclerView.adapter = spiderAdapter

        // Add Spider Button
        view.findViewById<ShapeableImageView>(R.id.addBtn).setOnClickListener {
            showAddSpiderDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.spiders.observe(viewLifecycleOwner) { spiders ->
            spiderAdapter.updateSpiders(spiders ?: emptyList())
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state
        }

        viewModel.addSpiderResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    addSpiderDialog?.dismiss()
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Failed to add spider: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showAddSpiderDialog() {
        Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_add_spider)
            
            // Setup spinners
            setupSpinners(this)

            findViewById<ImageButton>(R.id.i_close_btn).setOnClickListener {
                dismiss()
            }
            
            val imageContainer = findViewById<RelativeLayout>(R.id.dialog_spider_desc)
            val addButton = findViewById<Button>(R.id.dialog_add_spiderBtn)
            
            imageContainer.setOnClickListener {
                pickImage()
            }

            addButton.setOnClickListener {
                val name = findViewById<EditText>(R.id.dialog_spider_name).text.toString()
                val health = findViewById<Spinner>(R.id.dialog_spider_health).selectedItem.toString()
                val size = findViewById<Spinner>(R.id.dialog_spider_size).selectedItem.toString()
                val value = findViewById<EditText>(R.id.dialog_spider_value).text.toString().toDoubleOrNull() ?: 0.0
                val description = findViewById<EditText>(R.id.dialog_spider_description).text.toString()

                if (name.isNotBlank() && description.isNotBlank() && value > 0 && selectedImageUri != null) {
                    viewModel.addSpider(token, name, health, size, value, description, selectedImageUri!!, requireContext())
                } else {
                    Toast.makeText(context, getString(R.string.error_all_fields_required), Toast.LENGTH_SHORT).show()
                }
            }

            show()
            addSpiderDialog = this
        }
    }

    private fun setupSpinners(dialog: Dialog) {
        val healthSpinner = dialog.findViewById<Spinner>(R.id.dialog_spider_health)
        val sizeSpinner = dialog.findViewById<Spinner>(R.id.dialog_spider_size)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.health_status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            healthSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.size_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sizeSpinner.adapter = adapter
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            1001
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            addSpiderDialog?.let { dialog ->
                selectedImageUri?.let { uri ->
                    val imageView = dialog.findViewById<ImageView>(R.id.add_image_view)
                    Glide.with(requireContext())
                        .load(uri)
                        .into(imageView)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addSpiderDialog?.dismiss()
        addSpiderDialog = null
    }

    private fun showSpiderDetails(spider: Spider) {
        val detailsFragment = InventorySpiderDetailsFragment.newInstance(spider)
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailsFragment)
            .addToBackStack(null)
            .commit()
    }
}