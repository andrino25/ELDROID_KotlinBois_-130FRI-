package com.capstone.gagambrawl.model

data class Spider(
    val id: String,
    val spiderName: String,
    val spiderHealthStatus: String,
    val spiderSize: String,
    val spiderEstimatedMarketValue: Double,
    val spiderDescription: String,
    val spiderImageRef: String
)

data class SpiderRequest(
    val spiderName: String,
    val spiderHealthStatus: String,
    val spiderSize: String,
    val spiderEstimatedMarketValue: Double,
    val spiderDescription: String,
    val spiderImageRef: okhttp3.MultipartBody.Part
)