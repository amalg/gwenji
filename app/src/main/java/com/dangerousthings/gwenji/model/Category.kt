package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val icon: String,
    @SerialName("display_order") val displayOrder: Int
)
