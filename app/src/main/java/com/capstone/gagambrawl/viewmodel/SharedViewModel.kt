package com.capstone.gagambrawl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.gagambrawl.api.RetrofitClient
import com.capstone.gagambrawl.model.Spider
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val apiService = RetrofitClient.apiService
    private val _spiders = MutableLiveData<List<Spider>?>(null)
    val spiders: LiveData<List<Spider>?> = _spiders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSpiders(token: String) {
        if (_spiders.value != null) return

        fetchSpiders(token)
    }

    fun refreshSpiders(token: String) {
        fetchSpiders(token, isRefresh = true)
    }

    private fun fetchSpiders(token: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val spiderList = apiService.getSpiders(token)
                _spiders.value = spiderList
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                if (!isRefresh) {
                    _spiders.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSpiders() {
        _spiders.value = emptyList()
    }
}