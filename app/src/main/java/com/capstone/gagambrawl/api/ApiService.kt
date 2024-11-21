package com.capstone.gagambrawl.api

import com.capstone.gagambrawl.model.Catalog
import retrofit2.http.GET

interface ApiService {
    @GET("catalogs")
    suspend fun getCatalogs(): List<Catalog>
} 