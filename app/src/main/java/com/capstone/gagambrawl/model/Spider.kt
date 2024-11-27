package com.capstone.gagambrawl.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Spider(
    val spiderId: String,
    val userId: String,
    val spiderName: String,
    val spiderHealthStatus: String,
    val spiderSize: String,
    val spiderEstimatedMarketValue: Double,
    val spiderDescription: String,
    val spiderImageRef: String,
    val spiderIsFavorite: Int = 0,
    val created_at: String,
    val updated_at: String
) : Parcelable