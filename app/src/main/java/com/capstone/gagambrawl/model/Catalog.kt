package com.capstone.gagambrawl.model

data class Catalog(
    val catalogId: Int,
    val catalogName: String = "",
    val catalogDescription: String = "",
    val catalogImageRef: String
)
