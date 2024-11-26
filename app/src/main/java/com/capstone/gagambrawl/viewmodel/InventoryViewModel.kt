package com.capstone.gagambrawl.viewmodel

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.utils.NotificationHelper
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class InventoryViewModel : ViewModel() {
    private val _spiders = MutableLiveData<List<Spider>>()
    val spiders: LiveData<List<Spider>> = _spiders

    private val _currentFilter = MutableLiveData<FilterType>(FilterType.ALL)
    val currentFilter: LiveData<FilterType> = _currentFilter

    private val _filteredSpiders = MutableLiveData<List<Spider>>()
    val filteredSpiders: LiveData<List<Spider>> = _filteredSpiders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addSpiderResult = MutableLiveData<Result<String>?>()
    val addSpiderResult: LiveData<Result<String>?> = _addSpiderResult

    private val _deleteResult = MutableLiveData<Result<String>?>()
    val deleteResult: MutableLiveData<Result<String>?> = _deleteResult

    private val _updateSpiderResult = MutableLiveData<Result<Spider>?>()
    val updateSpiderResult: LiveData<Result<Spider>?> = _updateSpiderResult

    private val _favoriteToggleResult = MutableLiveData<Result<Spider>?>()
    val favoriteToggleResult: LiveData<Result<Spider>?> = _favoriteToggleResult

    enum class FilterType {
        ALL,
        FAVORITES
    }

    fun setFilter(filterType: FilterType) {
        _currentFilter.value = filterType
        applyFilter()
    }

    private fun applyFilter() {
        val currentSpiders = _spiders.value ?: emptyList()
        val filtered = when (_currentFilter.value) {
            FilterType.ALL -> currentSpiders
            FilterType.FAVORITES -> currentSpiders.filter { it.spiderIsFavorite == 1 }
            else -> currentSpiders
        }
        _filteredSpiders.value = filtered
    }

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gagambrawl-api.vercel.app/")
            .client(OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun loadSpiders(token: String) {
        viewModelScope.launch {
            try {
                val spiderList = apiService.getSpiders(token)
                _spiders.value = spiderList
                applyFilter() // Apply current filter to new data
            } catch (e: Exception) {
                _spiders.value = emptyList()
            }
        }
    }

    fun refreshSpiders(token: String) {
        viewModelScope.launch {
            try {
                val spiderList = apiService.getSpiders(token)
                _spiders.value = spiderList
                applyFilter() // Apply current filter to refreshed data
            } catch (e: Exception) {
                // Keep existing data on refresh failure
            }
        }
    }

    fun addSpider(
        token: String,
        name: String,
        health: String,
        size: String,
        value: Double,
        description: String,
        imageUri: Uri,
        context: Context,
        spiderIsFavorite: Int = 0,
        dialog: Dialog? = null
    ) {
        viewModelScope.launch {
            try {
                // Check if spider name and size combination already exists
                val existingSpiders = _spiders.value ?: emptyList()
                if (existingSpiders.any {
                    it.spiderName.lowercase() == name.lowercase() &&
                    it.spiderSize.lowercase() == size.lowercase()
                }) {
                    _addSpiderResult.value = Result.failure(Exception("A spider with this name and size already exists"))
                    Toast.makeText(context, "This spider already exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Create image file from URI
                val imageFile = imageUri.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg").apply {
                        outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                    }
                }

                // Create request bodies
                val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
                val healthPart = RequestBody.create("text/plain".toMediaTypeOrNull(), health)
                val sizePart = RequestBody.create("text/plain".toMediaTypeOrNull(), size)
                val valuePart = RequestBody.create("text/plain".toMediaTypeOrNull(), value.toString())
                val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
                val isFavoritePart = RequestBody.create("text/plain".toMediaTypeOrNull(), spiderIsFavorite.toString())
                val imagePart = MultipartBody.Part.createFormData(
                    "spiderImageRef",
                    imageFile.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                )

                // Make API call only if name doesn't exist
                val response = apiService.addSpider(
                    token,
                    namePart,
                    healthPart,
                    sizePart,
                    valuePart,
                    descriptionPart,
                    imagePart,
                    isFavoritePart
                )

                // Show notification
                NotificationHelper(context).showSpiderNotification(
                    spiderName = name,
                    action = NotificationHelper.SpiderAction.ADD,
                    spiderId = response.spiderId,
                    token = token
                )

                refreshSpiders(token)
                _addSpiderResult.value = Result.success("Spider added successfully")
                dialog?.dismiss()
            } catch (e: Exception) {
                _addSpiderResult.value = Result.failure(e)
            }
        }
    }

    fun clearSpiders() {
        _spiders.value = emptyList()
    }

    fun addSpider(
        token: String,
        name: String,
        health: String,
        size: String,
        value: Double,
        description: String,
        imageUri: Uri,
        context: Context,
        spiderIsFavorite: Int = 0
    ) {
        viewModelScope.launch {
            try {
                // Check if spider name already exists
                val existingSpiders = _spiders.value ?: emptyList()
                if (existingSpiders.any {
                        it.spiderName.toLowerCase().equals(name.toLowerCase(), ignoreCase = true) &&
                                it.spiderSize.toLowerCase().equals(size.toLowerCase(), ignoreCase = true)
                    }) {
                    _addSpiderResult.value = Result.failure(Exception("A spider with this name and size already exists"))
                    Toast.makeText(context, "This spider already exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }


                // Create image file from URI
                val imageFile = imageUri.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg").apply {
                        outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                    }
                }

                // Create request bodies
                val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
                val healthPart = RequestBody.create("text/plain".toMediaTypeOrNull(), health)
                val sizePart = RequestBody.create("text/plain".toMediaTypeOrNull(), size)
                val valuePart = RequestBody.create("text/plain".toMediaTypeOrNull(), value.toString())
                val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
                val isFavoritePart = RequestBody.create("text/plain".toMediaTypeOrNull(), spiderIsFavorite.toString())
                val imagePart = MultipartBody.Part.createFormData(
                    "spiderImageRef",
                    imageFile.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                )

                // Make API call only if name doesn't exist
                val response = apiService.addSpider(
                    token,
                    namePart,
                    healthPart,
                    sizePart,
                    valuePart,
                    descriptionPart,
                    imagePart,
                    isFavoritePart
                )

                // Show notification
                NotificationHelper(context).showSpiderNotification(
                    spiderName = name,
                    action = NotificationHelper.SpiderAction.ADD,
                    spiderId = response.spiderId,
                    token = token
                )

                refreshSpiders(token)
                _addSpiderResult.value = Result.success("Spider added successfully")
            } catch (e: Exception) {
                _addSpiderResult.value = Result.failure(e)
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun deleteSpider(token: String, spiderId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteSpider(token, spiderId)
                _deleteResult.value = Result.success(response.message)
                
                // Remove the deleted spider from the current list
                _spiders.value = _spiders.value?.filter { it.spiderId.toString() != spiderId }
                
                // Refresh the full list from the server
                refreshSpiders(token)
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }

    fun clearAddSpiderResult() {
        _addSpiderResult.value = null
    }

    fun updateSpider(
        token: String,
        spiderId: String,
        name: String?,
        health: String?,
        size: String?,
        value: Double?,
        description: String?,
        imageUri: Uri?,
        context: Context,
        originalSpider: Spider,
        dialog: Dialog? = null
    ) {
        viewModelScope.launch {
            try {
                // Check if any changes were made
                if (name == originalSpider.spiderName &&
                    health == originalSpider.spiderHealthStatus &&
                    size == originalSpider.spiderSize &&
                    value == originalSpider.spiderEstimatedMarketValue &&
                    description == originalSpider.spiderDescription &&
                    imageUri == null) {
                    _updateSpiderResult.value = Result.failure(
                        Exception("None of the information are changed. Edit unsuccessful.")
                    )
                    return@launch
                }

                // Check for duplicate name and size combination
                if (name != null && size != null) {
                    val existingSpiders = _spiders.value ?: emptyList()
                    val hasDuplicate = existingSpiders.any {
                        it.spiderId != spiderId && // Exclude current spider
                        it.spiderName.lowercase() == name.lowercase() &&
                        it.spiderSize.lowercase() == size.lowercase()
                    }
                    
                    if (hasDuplicate) {
                        _updateSpiderResult.value = Result.failure(Exception("A spider with this name and size already exists"))
                        Toast.makeText(context, "This spider already exists", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                // Create request bodies for changed fields only
                val method = RequestBody.create("text/plain".toMediaTypeOrNull(), "PATCH")
                val nameBody = name?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val healthBody = health?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val sizeBody = size?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val valueBody = value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }
                val descBody = description?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val isFavoriteBody = RequestBody.create("text/plain".toMediaTypeOrNull(), originalSpider.spiderIsFavorite.toString())

                // Handle image if changed
                var imagePart: MultipartBody.Part? = null
                if (imageUri != null) {
                    val imageFile = imageUri.let { uri ->
                        val inputStream = context.contentResolver.openInputStream(uri)
                        File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg").apply {
                            outputStream().use { output ->
                                inputStream?.copyTo(output)
                            }
                        }
                    }
                    imagePart = MultipartBody.Part.createFormData(
                        "spiderImageRef",
                        imageFile.name,
                        RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
                    )
                }

                val updatedSpider = apiService.updateSpider(
                    token,
                    spiderId,
                    method,
                    nameBody,
                    healthBody,
                    sizeBody,
                    valueBody,
                    descBody,
                    imagePart,
                    isFavoriteBody
                )

                // Show notification
                NotificationHelper(context).showSpiderNotification(
                    spiderName = name ?: originalSpider.spiderName,
                    action = NotificationHelper.SpiderAction.UPDATE,
                    spiderId = spiderId,
                    token = token
                )

                _updateSpiderResult.value = Result.success(updatedSpider)
                refreshSpiders(token)
                dialog?.dismiss()
            } catch (e: Exception) {
                _updateSpiderResult.value = Result.failure(e)
            }
        }
    }

    fun clearUpdateSpiderResult() {
        _updateSpiderResult.value = null
    }

    fun toggleFavorite(
        token: String, 
        spiderId: String, 
        isFavorite: Int, 
        spider: Spider, 
        context: Context,
        showNotification: Boolean = true  // Add parameter to control notification
    ) {
        viewModelScope.launch {
            try {
                val newFavoriteStatus = if (isFavorite == 0) 1 else 0
                
                val method = RequestBody.create("text/plain".toMediaTypeOrNull(), "PATCH")
                val favoriteBody = RequestBody.create("text/plain".toMediaTypeOrNull(), newFavoriteStatus.toString())

                val updatedSpider = apiService.updateSpider(
                    token,
                    spiderId,
                    method,
                    spiderIsFavorite = favoriteBody,
                    spiderName = null,
                    spiderHealthStatus = null,
                    spiderSize = null,
                    spiderEstimatedMarketValue = null,
                    spiderDescription = null,
                    spiderImageRef = null
                )

                // Only show notification if showNotification is true
                if (showNotification) {
                    NotificationHelper(context).showSpiderNotification(
                        spiderName = spider.spiderName,
                        action = NotificationHelper.SpiderAction.UPDATE,
                        spiderId = spiderId,
                        token = token
                    )
                }

                _favoriteToggleResult.value = Result.success(updatedSpider)
                refreshSpiders(token)
            } catch (e: Exception) {
                _favoriteToggleResult.value = Result.failure(e)
            }
        }
    }

    fun clearFavoriteToggleResult() {
        _favoriteToggleResult.value = null
    }
}