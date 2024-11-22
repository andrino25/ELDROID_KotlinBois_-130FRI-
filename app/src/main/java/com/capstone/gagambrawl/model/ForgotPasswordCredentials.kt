package com.capstone.gagambrawl.model

data class ForgotPasswordCredentials(
    val email: String,
    val new_password: String,
    val new_password_confirmation: String
)
