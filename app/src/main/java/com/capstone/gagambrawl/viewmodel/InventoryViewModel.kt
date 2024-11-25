package com.capstone.gagambrawl.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.api.ApiService
import com.capstone.gagambrawl.model.Spider
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addSpiderResult = MutableLiveData<Result<String>?>()
    val addSpiderResult: LiveData<Result<String>?> = _addSpiderResult

    private val _deleteResult = MutableLiveData<Result<String>?>()
    val deleteResult: MutableLiveData<Result<String>?> = _deleteResult

    private val _updateSpiderResult = MutableLiveData<Result<Spider>?>()
    val updateSpiderResult: LiveData<Result<Spider>?> = _updateSpiderResult

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
        if (_spiders.value != null && _spiders.value?.isNotEmpty() == true) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val spiderList = apiService.getSpiders(token)
                _spiders.value = spiderList
            } catch (e: Exception) {
                _spiders.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSpiders(token: String) {
        viewModelScope.launch {
            try {
                val spiderList = apiService.getSpiders(token)
                _spiders.value = spiderList
            } catch (e: Exception) {
                // Keep existing data on refresh failure
            }
        }
    }

    fun addSpider(spider: Spider) {
        val currentList = _spiders.value?.toMutableList() ?: mutableListOf()
        currentList.add(spider)
        _spiders.value = currentList
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
        context: Context
    ) {
        viewModelScope.launch {
            try {
                // Create image file from URI
                val imageFile = imageUri.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg").apply {
                        outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                    }
                }

                // Create multipart request
                val imagePart = MultipartBody.Part.createFormData(
                    "spiderImageRef",
                    imageFile.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                )

                // Add spider
                apiService.addSpider(
                    token,
                    RequestBody.create("text/plain".toMediaTypeOrNull(), name),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), health),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), size),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), value.toString()),
                    RequestBody.create("text/plain".toMediaTypeOrNull(), description),
                    imagePart
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
        originalSpider: Spider
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

                // Create request bodies for changed fields only
                val method = RequestBody.create("text/plain".toMediaTypeOrNull(), "PATCH")
                val nameBody = name?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val healthBody = health?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val sizeBody = size?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
                val valueBody = value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }
                val descBody = description?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

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
                        RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
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
                    imagePart
                )

                _updateSpiderResult.value = Result.success(updatedSpider)
                refreshSpiders(token)
            } catch (e: Exception) {
                _updateSpiderResult.value = Result.failure(e)
            }
        }
    }

    fun clearUpdateSpiderResult() {
        _updateSpiderResult.value = null
    }
}