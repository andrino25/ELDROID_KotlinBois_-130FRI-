package com.capstone.gagambrawl.api

import com.capstone.gagambrawl.model.Catalog
import com.capstone.gagambrawl.model.Spider
import com.capstone.gagambrawl.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.DELETE

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

    @GET("api/api/spiders")
    suspend fun getSpiders(@Header("Authorization") token: String): List<Spider>

    @Multipart
    @POST("api/api/spiders")
    suspend fun addSpider(
        @Header("Authorization") token: String,
        @Part("spiderName") spiderName: RequestBody,
        @Part("spiderHealthStatus") spiderHealthStatus: RequestBody,
        @Part("spiderSize") spiderSize: RequestBody,
        @Part("spiderEstimatedMarketValue") spiderEstimatedMarketValue: RequestBody,
        @Part("spiderDescription") spiderDescription: RequestBody,
        @Part spiderImageRef: MultipartBody.Part,
        @Part("spiderIsFavorite") spiderIsFavorite: RequestBody
    ): Spider

    @GET("api/api/spiders/{spiderId}")
    suspend fun getSpiderById(
        @Header("Authorization") token: String,
        @Path("spiderId") spiderId: String
    ): Spider

    @DELETE("api/api/spiders/{spiderId}")
    suspend fun deleteSpider(
        @Header("Authorization") token: String,
        @Path("spiderId") spiderId: String
    ): DeleteResponse

    @Multipart
    @POST("api/api/user/edit")
    suspend fun updateUserProfileWithImage(
        @Header("Authorization") token: String,
        @Part("_method") method: RequestBody,
        @Part("userFirstName") firstName: RequestBody?,
        @Part("userMiddleName") middleName: RequestBody?,
        @Part("userLastName") lastName: RequestBody?,
        @Part("userAddress") address: RequestBody?,
        @Part userProfilePicRef: MultipartBody.Part?
    ): User

    @Multipart
    @POST("api/api/spiders/{spiderId}")
    suspend fun updateSpider(
        @Header("Authorization") token: String,
        @Path("spiderId") spiderId: String,
        @Part("_method") method: RequestBody,
        @Part("spiderName") spiderName: RequestBody?,
        @Part("spiderHealthStatus") spiderHealthStatus: RequestBody?,
        @Part("spiderSize") spiderSize: RequestBody?,
        @Part("spiderEstimatedMarketValue") spiderEstimatedMarketValue: RequestBody?,
        @Part("spiderDescription") spiderDescription: RequestBody?,
        @Part spiderImageRef: MultipartBody.Part?,
        @Part("spiderIsFavorite") spiderIsFavorite: RequestBody?
    ): Spider

    data class DeleteResponse(
        val message: String
    )
} 