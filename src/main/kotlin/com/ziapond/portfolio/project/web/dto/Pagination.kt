package com.ziapond.portfolio.project.web.dto


data class Pagination(
    val totalItems: Long,
    val currentPage: Int,
    val totalPages: Int,
    val limit: Int
)