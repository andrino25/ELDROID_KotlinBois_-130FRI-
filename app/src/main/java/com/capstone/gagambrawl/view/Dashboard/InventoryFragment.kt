package com.capstone.gagambrawl.view.Dashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
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
import android.widget.TextView
import com.capstone.gagambrawl.utils.SessionManager


class InventoryFragment : Fragment() {
    private var token: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var spiderAdapter: SpiderAdapter
    private var addSpiderDialog: Dialog? = null
    private val viewModel: InventoryViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null
    private var isUserAction = false
    private var loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        // Initialize token
        token = arguments?.getString("token") ?:
                activity?.intent?.getStringExtra("token") ?: ""
        if (!token.startsWith("Bearer ")) {
            token = "Bearer $token"
        }

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.spidersRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        spiderAdapter = SpiderAdapter(
            emptyList(),
            onSpiderClick = { spider -> showSpiderDetails(spider) },
            onFavoriteClick = { spider -> 
                isUserAction = true
                viewModel.toggleFavorite(
                    token = token,
                    spiderId = spider.spiderId,
                    isFavorite = spider.spiderIsFavorite,
                    spider = spider,
                    context = requireContext()
                )
            }
        )
        recyclerView.adapter = spiderAdapter

        // Setup menu button
        view.findViewById<ImageButton>(R.id.menuHeader).setOnClickListener { button ->
            showFilterMenu(button)
        }

        // Setup add button
        view.findViewById<ShapeableImageView>(R.id.addBtn).setOnClickListener {
            showAddSpiderDialog()
        }

        return view
    }

    private fun showFilterMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.filter_menu, menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.filter_all -> {
                        viewModel.setFilter(InventoryViewModel.FilterType.ALL)
                        true
                    }
                    R.id.filter_favorites -> {
                        viewModel.setFilter(InventoryViewModel.FilterType.FAVORITES)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        // Observe filtered spiders
        viewModel.filteredSpiders.observe(viewLifecycleOwner) { spiders ->
            // Show/hide empty state based on spiders list
            view?.findViewById<TextView>(R.id.empty_inventory_text)?.visibility =
                if (spiders.isEmpty()) View.VISIBLE else View.GONE
            view?.findViewById<RecyclerView>(R.id.spidersRecyclerView)?.visibility = 
                if (spiders.isEmpty()) View.GONE else View.VISIBLE
            
            spiderAdapter.updateSpiders(spiders)

            // Handle target spider if present
            val targetSpiderName = arguments?.getString("target_spider_name")
            if (!targetSpiderName.isNullOrEmpty()) {
                spiders.find { it.spiderName == targetSpiderName }?.let { spider ->
                    view?.postDelayed({
                        showSpiderDetails(spider)
                        arguments?.remove("target_spider_name")
                        arguments?.remove("notification_action")
                    }, 300)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state
        }

        viewModel.isAddingSpider.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoadingDialog()
            } else {
                loadingDialog?.dismiss()
            }
        }

        // ... other observers ...
    }

    override fun onResume() {
        super.onResume()
        val token = arguments?.getString("token") ?:
                    activity?.intent?.getStringExtra("token") ?:
                    SessionManager(requireContext()).fetchAuthToken() ?: ""

        if (!token.startsWith("Bearer ")) {
            this.token = "Bearer $token"
        }

        viewModel.refreshSpiders(token)
    }

    private fun showSpiderDetails(spider: Spider) {
        val detailsFragment = InventorySpiderDetailsFragment.newInstance(spider)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        addSpiderDialog?.dismiss()
        addSpiderDialog = null
    }

    private fun showAddSpiderDialog() {
        addSpiderDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_add_spider)

            val selectImageBtn = findViewById<ImageView>(R.id.add_image_view)
            val healthSpinner = findViewById<Spinner>(R.id.dialog_spider_health)
            val sizeSpinner = findViewById<Spinner>(R.id.dialog_spider_size)

            findViewById<ImageButton>(R.id.i_close_btn).setOnClickListener {
                dismiss()
            }

            // Setup spinners
            ArrayAdapter.createFromResource(
                context,
                R.array.health_status_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                healthSpinner.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                context,
                R.array.size_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sizeSpinner.adapter = adapter
            }

            // Image selection
            selectImageBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    PICK_IMAGE_REQUEST
                )
            }

            // Add spider button
            findViewById<Button>(R.id.dialog_add_spiderBtn).setOnClickListener {
                val name = findViewById<EditText>(R.id.dialog_spider_name).text.toString()
                val health = healthSpinner.selectedItem.toString()
                val size = sizeSpinner.selectedItem.toString()
                val value = findViewById<EditText>(R.id.dialog_spider_value).text.toString().toDoubleOrNull() ?: 0.0
                val description = findViewById<EditText>(R.id.dialog_spider_description).text.toString()

                if (name.isBlank() || selectedImageUri == null) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewModel.addSpider(
                    token = token,
                    name = name,
                    health = health,
                    size = size,
                    value = value,
                    description = description,
                    imageUri = selectedImageUri!!,
                    context = requireContext(),
                    spiderIsFavorite = 0,
                    dialog = addSpiderDialog

                )
            }

            show()
            addSpiderDialog = this
        }
    }

    private val PICK_IMAGE_REQUEST = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
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

    private fun showLoadingDialog() {
        loadingDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.pre_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }
}