package com.megamind.StockManagerApi.utlis

import org.springframework.data.domain.Page

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val pageNumber: Int,
    val pageSize: Int
)