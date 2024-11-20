package com.capstone.gagambrawl.model

data class User(
    val id: Int,
    val email: String,
    val email_verified_at: String?,
    val userFirstName: String?,
    val userMiddleName: String?,
    val userLastName: String?,
    val userAddress: String?,
    val userProfilePicRef: String?,
    val created_at: String,
    val updated_at: String
)
