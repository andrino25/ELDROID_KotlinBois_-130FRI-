package com.capstone.gagambrawl.api

import com.capstone.gagambrawl.model.Catalog
import com.capstone.gagambrawl.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @GET("catalogs")
    suspend fun getCatalogs(): List<Catalog>

    @GET("api/api/user")
    suspend fun getUserProfile(@Header("Authorization") token: String): User
    
    @POST("api/api/user/edit")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body userUpdate: Map<String, String?>
    ): User
} 