package com.ziapond.portfolio.project.dto


data class Pagination(
    val totalItems: Long,
    val currentPage: Int,
    val totalPages: Int,
    val limit: Int
)