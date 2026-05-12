package com.diegocunha.thenaapp.datasource.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    @SerialName("content")
    val content: List<T>,
    @SerialName("total_elements")
    val totalElements: Long,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("number")
    val number: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("last")
    val last: Boolean,
)
